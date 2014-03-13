/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.cmd;

import static org.o42a.util.string.Capitalization.AS_IS;

import java.util.List;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.st.Command;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


final class SentencesOp {

	private static final Name BLK_SUFFIX = AS_IS.name("_blk");
	private static final Name NEXT_SUFFIX = AS_IS.name("_next");

	static void writeSentences(
			Control control,
			Scope origin,
			Sentences sentences,
			InlineSentences inline) {

		final ID name = control.name(sentences.getName()).suffix(BLK_SUFFIX);
		final Block code = control.addBlock(name);
		final Block next = control.addBlock(name.suffix(NEXT_SUFFIX));
		final Control blockControl;

		if (sentences.isParentheses()) {
			blockControl =
					control.parentheses(code, next.head());
		} else {
			blockControl =
					control.braces(code, next.head(), sentences.getName());
		}

		final List<? extends Sentence> sentenceList =
				sentences.getSentences();
		final int len = sentenceList.size();

		for (int i = 0; i < len; ++i) {

			final Sentence sentence = sentenceList.get(i);

			writeSentence(
					blockControl,
					origin,
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
			Scope origin,
			Sentence sentence,
			InlineSentence inline,
			String index) {

		final Sentence prerequisite = sentence.getPrerequisite();
		final Block prereqFailed;

		if (prerequisite == null) {
			prereqFailed = null;
		} else {
			// write prerequisite
			prereqFailed = control.addBlock(index + "_prereq_failed");

			final Control prereqControl = control.interrogation(prereqFailed.head());

			writeSentence(
					prereqControl,
					origin,
					prerequisite,
					inline != null ? inline.getPrerequisite() : null,
					index + "_prereq");

			prereqControl.end();
		}

		final List<Statements> alternatives = sentence.getAlternatives();
		final int len = alternatives.size();

		if (len <= 1) {
			if (len != 0) {
				writeStatements(
						control,
						origin,
						alternatives.get(0),
						inline != null ? inline.get(0) : null);
			}
			endPrereq(control, prereqFailed);
			endAlt(sentence, control, control);
			return;
		}

		// code blocks for each alternative
		final Block[] blocks = new Block[len];
		final ID sentId = ID.id(index + "_sent");

		for (int i = 0; i < len; ++i) {
			blocks[i] = control.addBlock(sentId.sub(i + "_alt"));
		}
		control.code().go(blocks[0].head());
		endPrereq(control, prereqFailed);

		// fill code blocks
		for (int i = 0; i < len; ++i) {

			final Statements alt = alternatives.get(i);
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
					origin,
					alt,
					inline != null ? inline.get(i) : null);

			altControl.end();

			endAlt(sentence, control, altControl);
		}
	}

	private static void writeStatements(
			Control control,
			Scope origin,
			Statements statements,
			InlineCommands inlines) {

		final List<Command> commands = statements.getCommands();
		final int size = commands.size();

		for (int i = 0; i < size; ++i) {

			final Command command = commands.get(i);

			if (inlines != null) {

				final InlineCmd inline = inlines.get(i);

				if (inline != null) {
					inline.write(control);
					continue;
				}
			}

			command.cmd(origin).write(control);
		}
	}

	private static void endPrereq(Control control, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private static void endAlt(
			Sentence sentence,
			Control mainControl,
			Control control) {
		if (sentence.getKind().isExclamatory()) {
			control.exitBraces();
			return;
		}
		// Interrogative condition satisfied or sentence successfully complete.
		// Go to the next sentence.
		if (control.code() != mainControl.code()) {
			control.code().go(mainControl.code().tail());
		}
	}

	private SentencesOp() {
	}

}
