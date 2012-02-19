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
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;


final class ImperativeOp {

	public static void writeSentences(
			Control control,
			ValOp result,
			ImperativeBlock block,
			InlineBlock inline) {

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
					result,
					sentence,
					inline != null ? inline.get(i) : null,
					Integer.toString(i));
			if (!blockControl.mayContinue()) {
				blockControl.end();
				control.reachability(blockControl);
				return;
			}
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

		control.reachability(blockControl);
	}

	private static void writeSentence(
			Control control,
			ValOp result,
			ImperativeSentence sentence,
			InlineSentence inline,
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
					null,
					prerequisite,
					inline != null ? inline.getPrerequisite() : null,
					index + "_prereq");

			prereqControl.end();

			control.reachability(prereqControl);
			if (!prereqControl.mayContinue()) {
				return;
			}
		}

		final List<Imperatives> alternatives = sentence.getAlternatives();
		final int len = alternatives.size();

		if (len <= 1) {
			if (len != 0) {
				writeStatements(
						control,
						result,
						alternatives.get(0),
						inline != null ? inline.get(0) : null);
			}
			endPrereq(control, prereqFailed);
			end(sentence, control, control);
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

		Control nextAltReachability = control;
		Control nextOppUnreachability = control;

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

			if (alt.isOpposite()) {
				altControl.reachability(nextOppUnreachability);
			} else {
				nextOppUnreachability = control;
				// all preceding opposites are exited unconditionally
				control.reachability(nextAltReachability);
				altControl.reachability(nextAltReachability);
			}

			writeStatements(
					altControl,
					result,
					alt,
					inline != null ? inline.get(i) : null);

			if (!altControl.mayContinue()) {
				altControl.end();
				control.reachability(altControl);
				break;
			}
			if (control.isDone()) {
				altControl.end();
				continue;
			}
			if (altControl.isDone()) {
				altControl.end();
				if (alt.getDefiners().size() == 1) {
					// the only statement is exit
					if (sentence.hasOpposite(i)) {// one of the opposites
						nextOppUnreachability = altControl;
						if (!alt.isOpposite()) {// first opposite
							nextAltReachability = altControl;
						}
					} else {
						// non-opposing alternative is done
						// subsequent statements not reachable
						control.reachability(altControl);
					}
				}
				continue;
			}

			nextAltReachability = control;

			// skip ascending opposites
			final int nextNonOppositeIdx =
					nextNonOppositeIdx(sentence, nextIdx);

			if (nextNonOppositeIdx >= 0) {
				// execute next non-opposing alternative,
				// as all such alternatives expected to be executed
				altCode.go(blocks[nextNonOppositeIdx].head());
			} else {
				// everything is successfully done
				endPrereq(control, prereqFailed);
				end(sentence, control, altControl);
			}
			altControl.end();
		}

		if (prerequisite == null) {
			control.reachability(nextAltReachability);
		}
	}

	private static void writeStatements(
			Control control,
			ValOp result,
			Imperatives statements,
			InlineCommands inlines) {

		final List<Definer> definers = statements.getDefiners();
		final int size = definers.size();

		for (int i = 0; i < size; ++i) {

			final Statement statement = definers.get(i).getStatement();

			if (!control.reach(statement)) {
				return;
			}
			if (inlines == null) {

				final Cmd cmd = statement.cmd(control.getBuilder());

				cmd.write(control, result);
			} else {
				inlines.get(i).write(control, result);
			}
		}
	}

	private static int nextNonOppositeIdx(
			ImperativeSentence sentence,
			int start) {

		final List<Imperatives> alternatives = sentence.getAlternatives();
		final int len = alternatives.size();

		for (int i = start; i < len; ++i) {
			if (!alternatives.get(i).isOpposite()) {
				return i;
			}
		}

		return -1;
	}

	private static void endPrereq(Control control, Block prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private static void end(
			ImperativeSentence sentence,
			Control mainControl,
			Control control) {
		if (control.isDone()) {
			return;
		}
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
