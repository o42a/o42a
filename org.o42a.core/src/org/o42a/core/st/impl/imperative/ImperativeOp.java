/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.st.impl.imperative;

import java.util.List;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.st.Command;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;


final class ImperativeOp {

	public static void writeSentences(
			Control control,
			ImperativeBlock block,
			InlineImperativeBlock inline) {

		final String name = control.name(block.getName()) + "_blk";

		final Block code = control.addBlock(name);
		final Block next = control.addBlock(name + "_next");
		final Control blockControl;

		if (block.isParentheses()) {
			blockControl = control.parentheses(code, next.head());
		} else {
			blockControl = control.braces(code, next.head(), name);
		}

		final List<ImperativeSentence> sentences = block.getSentences();
		final int len = sentences.size();

		for (int i = 0; i < len; ++i) {

			final ImperativeSentence sentence = sentences.get(i);

			writeSentence(
					blockControl,
					sentence,
					inline != null ? inline.get(i) : null,
					Integer.toString(i));
		}

		blockControl.end();

		if (code.created()) {
			control.code().go(code.head());
			if (next.exists()) {
				next.go(control.code().tail());
			}
			if (code.exists()) {
				code.go(control.code().tail());
			}
		}
	}

	private static void writeSentence(
			Control control,
			ImperativeSentence sentence,
			InlineImperativeSentence inline,
			String index) {

		final ImperativeSentence prerequisite = sentence.getPrerequisite();
		final Block prereqFailed;

		if (prerequisite == null) {
			prereqFailed = null;
		} else {
			// write prerequisite
			prereqFailed = control.addBlock(index + "_prereq_failed");

			final Control prereqControl = control.issue(prereqFailed.head());

			writeSentence(
					prereqControl,
					prerequisite,
					inline != null ? inline.getPrerequisite() : null,
					index + "_prereq");

			prereqControl.end();
		}

		final List<Imperatives> alternatives = sentence.getAlternatives();
		final int len = alternatives.size();

		if (len <= 1) {
			if (len != 0) {
				writeStatements(
						control,
						alternatives.get(0),
						inline != null ? inline.get(0) : null);
			}
			endPrereq(control, prereqFailed);
			endAlt(sentence, control, control);
			return;
		}

		// code blocks for each alternative
		final Block[] blocks = new Block[len];
		final Generator generator = control.getGenerator();
		final CodeId sentId = generator.id(index + "_sent");

		for (int i = 0; i < len; ++i) {
			blocks[i] = control.addBlock(sentId.sub(i + "_alt"));
		}
		control.code().go(blocks[0].head());
		endPrereq(control, prereqFailed);

		// fill code blocks
		for (int i = 0; i < len; ++i) {

			final Imperatives alt = alternatives.get(i);
			final Block altCode = blocks[i];
			final Control altControl;
			final int nextIdx = i + 1;

			if (nextIdx >= len) {
				// last alternative
				altControl = control.alt(altCode, control.exit());
			} else {
				altControl = control.alt(altCode, blocks[nextIdx].head());
			}

			writeStatements(
					altControl,
					alt,
					inline != null ? inline.get(i) : null);

			altControl.end();

			endAlt(sentence, control, altControl);
		}
	}

	private static void writeStatements(
			Control control,
			Imperatives statements,
			InlineCommands inlines) {

		final List<Command> commands = statements.getImplications();
		final int size = commands.size();

		for (int i = 0; i < size; ++i) {

			final Command command = commands.get(i);

			if (inlines == null) {

				final Cmd cmd = command.cmd(control.getBuilder());

				cmd.write(control);
			} else {
				inlines.get(i).write(control);
			}
		}
	}

	private static void endPrereq(Control control, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private static void endAlt(
			ImperativeSentence sentence,
			Control mainControl,
			Control control) {
		if (sentence.isClaim()) {
			// claim - exit block
			control.exitBraces();
			return;
		}
		// issue condition satisfied or proposition successfully complete
		// go to the next sentence
		if (control.code() != mainControl.code()) {
			control.code().go(mainControl.code().tail());
		}
	}

	private ImperativeOp() {
	}

}
