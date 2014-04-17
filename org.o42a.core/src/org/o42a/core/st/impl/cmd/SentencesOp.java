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

import java.util.List;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.st.Command;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;


final class SentencesOp {

	static void writeSentences(
			Control control,
			Scope origin,
			Sentences sentences,
			InlineSentences inline) {

		final SentenceIndex index = new SentenceIndex(control, origin);
		final Control blockControl = index.startBlock(sentences);
		final List<? extends Sentence> sentenceList = sentences.getSentences();
		final int len = sentenceList.size();

		for (int i = 0; i < len; ++i) {

			final Sentence sentence = sentenceList.get(i);

			writeSentence(
					blockControl,
					sentence,
					inline != null ? inline.get(i) : null,
					Integer.toString(i),
					index);
		}

		index.endBlock();
	}

	private static void writeSentence(
			Control control,
			Sentence sentence,
			InlineSentence inline,
			String prefix,
			SentenceIndex index) {

		final Block prereqFailed =
				writePrereq(control, sentence, inline, prefix, index);
		final List<Statements> alternatives = sentence.getAlternatives();
		final int size = alternatives.size();

		index.startSentence(control, prefix, size);
		endPrereq(control, prereqFailed);

		// fill code blocks
		for (int i = 0; i < size; ++i) {

			final Control altControl = index.startAlt(control, i);

			writeStatements(
					altControl,
					alternatives.get(i),
					inline != null ? inline.get(i) : null,
					index);

			index.endAlt(sentence, control);
		}

		index.endSentence();
	}

	private static Block writePrereq(
			Control control,
			Sentence sentence,
			InlineSentence inline,
			String prefix,
			SentenceIndex index) {

		final Sentence prerequisite = sentence.getPrerequisite();

		if (prerequisite == null) {
			return null;
		}
			// write prerequisite
		final Block prereqFailed =
				control.addBlock(prefix + "_prereq_failed");
		final Control prereqControl =
				control.interrogation(prereqFailed.head());

		writeSentence(
				prereqControl,
				prerequisite,
				inline != null ? inline.getPrerequisite() : null,
				prefix + "_prereq",
				new SentenceIndex(prereqControl, index.getOrigin()));

		prereqControl.end();

		return prereqFailed;
	}

	private static void endPrereq(Control control, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private static void writeStatements(
			Control control,
			Statements statements,
			InlineCommands inlines,
			SentenceIndex index) {

		final List<Command> commands = statements.getCommands();
		final int size = commands.size();

		for (int i = 0; i < size; ++i) {
			statementCmd(statements, inlines, index, i).write(control);
		}
	}

	private static Cmd statementCmd(
			Statements statements,
			InlineCommands inlines,
			SentenceIndex index,
			int i) {
		if (inlines != null) {

			final InlineCmd inline = inlines.get(i);

			if (inline != null) {
				return inline;
			}
		}

		final Command command = statements.getCommands().get(i);

		return command.cmd(index.getOrigin());
	}

	private SentencesOp() {
	}

}
