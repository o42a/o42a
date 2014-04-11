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

import static org.o42a.core.ir.cmd.CmdResult.CMD_DONE;
import static org.o42a.core.ir.cmd.CmdResult.CMD_NEXT;
import static org.o42a.core.ir.cmd.CmdResult.CMD_REPEAT;

import java.util.List;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.*;
import org.o42a.core.st.Command;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;


final class SentencesOp {

	static void writeSentences(
			Control control,
			Scope origin,
			Sentences sentences,
			InlineSentences inline,
			CmdState<SentenceIndex> state) {

		final SentenceIndex index = sentenceIndex(control, origin, state);
		final Control blockControl = index.startBlock(sentences);

		CmdResult result = CMD_DONE;
		final List<? extends Sentence> sentenceList = sentences.getSentences();
		final int len = sentenceList.size();

		for (; index.getSentence() < len; index.nextSentence()) {

			final int i = index.getSentence();
			final Sentence sentence = sentenceList.get(i);
			final CmdResult res = writeSentence(
					blockControl,
					sentence,
					inline != null ? inline.get(i) : null,
					Integer.toString(i),
					index);

			if (res.isDone()) {
				continue;
			}
			if (res.isRepeat()) {
				result = res;
				break;
			}
			if (i + 1 >= len) {
				// Last sentence. Go to the next statement.
				result = CMD_NEXT;
				break;
			}
			// Not the last sentence.
			// Repeat starting from the next one.
			index.nextSentence();
			result = CMD_REPEAT;
			break;
		}

		updateState(state, index, result);
	}

	private static SentenceIndex sentenceIndex(
			Control control,
			Scope origin,
			CmdState<SentenceIndex> state) {

		final SentenceIndex index = state.get();

		if (index != null) {
			return index;
		}

		return new SentenceIndex(control, origin);
	}

	private static void updateState(
			CmdState<SentenceIndex> state,
			SentenceIndex index,
			CmdResult result) {
		switch (result) {
		case CMD_DONE:
			index.endBlock();
			state.done();
			return;
		case CMD_NEXT:
			index.endBlock();
			state.next();
			return;
		case CMD_REPEAT:
			state.repeat(index);
			return;
		}
		throw new IllegalStateException(
				"Unexpected copmmand result: " + result);
	}

	private static CmdResult writeSentence(
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
		for (; index.getAlt() < size; index.nextAlt()) {

			final int i = index.getAlt();
			final Control altControl = index.startAlt(control);
			final CmdResult result = writeStatements(
					altControl,
					alternatives.get(i),
					inline != null ? inline.get(i) : null,
					index);

			if (result.isDone()) {
				index.endAlt(sentence, control);
				continue;
			}
			if (result.isRepeat()) {
				return CMD_REPEAT;
			}
			index.endAlt(sentence, control);
			if (i + 1 < size) {
				// Not the last alternative. Repeat from the next one.
				index.nextAlt();
				return CMD_REPEAT;
			}
			// Last alternative. Repeat from the next statement.
			index.endSentence();
			return CMD_NEXT;
		}

		index.endSentence();

		return CMD_DONE;
	}

	private static Block writePrereq(
			Control control,
			Sentence sentence,
			InlineSentence inline,
			String prefix,
			SentenceIndex index) {

		final Sentence prerequisite = sentence.getPrerequisite();

		if (prerequisite == null || index.isPrereqWritten()) {
			return null;
		}
			// write prerequisite
		final Block prereqFailed =
				control.addBlock(prefix + "_prereq_failed");
		final Control prereqControl =
				control.interrogation(prereqFailed.head());
		final CmdResult result = writeSentence(
				prereqControl,
				prerequisite,
				inline != null ? inline.getPrerequisite() : null,
				prefix + "_prereq",
				new SentenceIndex(prereqControl, index.getOrigin()));

		assert result.isDone() :
			"Unexpected prerequisite result: " + result;

		prereqControl.end();

		index.writePrereq();

		return prereqFailed;
	}

	private static void endPrereq(Control control, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private static CmdResult writeStatements(
			Control control,
			Statements statements,
			InlineCommands inlines,
			SentenceIndex index) {

		final List<Command> commands = statements.getCommands();
		final int size = commands.size();

		for (; index.getStatement() < size; index.nextStatement()) {

			final Cmd<?> cmd = statementCmd(statements, inlines, index);
			final CmdResult result = control.write(cmd);

			if (!result.isDone()) {
				return result;
			}
		}

		return CMD_DONE;
	}

	private static Cmd<?> statementCmd(
			Statements statements,
			InlineCommands inlines,
			SentenceIndex index) {

		final int i = index.getStatement();

		if (inlines != null) {

			final InlineCmd<?> inline = inlines.get(i);

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
