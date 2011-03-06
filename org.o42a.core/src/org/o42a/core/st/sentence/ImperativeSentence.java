/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.StatementKinds.NO_STATEMENTS;

import java.util.List;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.LocationInfo;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.StatementKinds;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.value.LogicalValue;


public abstract class ImperativeSentence extends Sentence<Imperatives> {

	private StatementKinds statementKinds;

	ImperativeSentence(
			LocationInfo location,
			ImperativeBlock block,
			ImperativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public ImperativeBlock getBlock() {
		return (ImperativeBlock) super.getBlock();
	}

	@Override
	public ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@Override
	public ImperativeSentence getPrerequisite() {
		return (ImperativeSentence) super.getPrerequisite();
	}

	@Override
	public StatementKinds getStatementKinds() {
		if (this.statementKinds != null) {
			return this.statementKinds;
		}

		StatementKinds result = NO_STATEMENTS;

		for (Imperatives alt : getAlternatives()) {
			result = result.add(alt.getStatementKinds());
		}

		return this.statementKinds = result;
	}

	protected void allocate(LocalBuilder builder, Code code) {
		for (Imperatives alt : getAlternatives()) {
			alt.allocate(builder, code);
		}
	}

	protected Action initialValue(LocalScope scope) {

		final List<Imperatives> alternatives = getAlternatives();
		final int size = alternatives.size();
		Action result = null;

		for (int i = 0; i < size; ++i) {

			final Imperatives alt = alternatives.get(i);
			final Action action = alt.initialValue(scope);

			if (action.isAbort()) {
				return action;
			}

			final LogicalValue logicalValue = action.getLogicalValue();

			if (!logicalValue.isConstant()) {
				// can not go on
				return action;
			}
			if (!alt.isOpposite()) {
				if (result != null && !result.getLogicalValue().isTrue()) {
					return result;
				}
			} else if (!hasOpposite(i)) {
				if (!action.getLogicalValue().isTrue()) {
					return action;
				}
			}

			result = action;
		}

		if (isClaim()) {
			return new ExitLoop(this, null);
		}
		if (result != null) {
			return result;
		}

		return new ExecuteCommand(this, LogicalValue.TRUE);
	}

	void write(Control control, String index, ValOp result) {

		final ImperativeSentence prerequisite = getPrerequisite();
		final Code prereqFailed;

		if (prerequisite == null) {
			prereqFailed = null;
		} else {
			// write prerequisite
			prereqFailed = control.addBlock(index + "_prereq_failed");

			final Control prereqControl = control.issue(prereqFailed.head());

			prerequisite.write(prereqControl, index + "_prereq", null);
			control.reachability(prereqControl);
			if (!prereqControl.mayContinue()) {
				return;
			}
		}

		final List<Imperatives> alternatives = getAlternatives();
		final int len = alternatives.size();

		if (len <= 1) {
			if (len != 0) {
				alternatives.get(0).write(control, result);
			}
			endPrereq(control, prereqFailed);
			end(control, control);
			return;
		}

		// code blocks for each alternative
		final Code[] blocks = new Code[len];
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
			final Code altCode = blocks[i];
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

			alt.write(altControl, result);

			if (!altControl.mayContinue()) {
				control.reachability(altControl);
				break;
			}
			if (control.isDone()) {
				continue;
			}
			if (altControl.isDone()) {
				if (alt.getStatements().size() == 1) {
					// the only statement is exit
					if (hasOpposite(i)) {// one of the opposites
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
			final int nextNonOppositeIdx = nextNonOppositeIdx(nextIdx);

			if (nextNonOppositeIdx >= 0) {
				// execute next non-opposing alternative,
				// as all such alternatives expected to be executed
				altCode.go(blocks[nextNonOppositeIdx].head());
			} else {
				// everything is successfully done
				endPrereq(control, prereqFailed);
				end(control, altControl);
			}
		}

		if (prerequisite == null) {
			control.reachability(nextAltReachability);
		}
	}

	private boolean hasOpposite(int altIdx) {

		final List<Imperatives> alternatives = getAlternatives();
		final Imperatives alt = alternatives.get(altIdx);

		if (alt.isOpposite()) {
			return true;
		}

		final int nextIdx = altIdx + 1;

		if (nextIdx >= alternatives.size()) {
			return false;
		}

		return alternatives.get(nextIdx + 1).isOpposite();
	}

	private int nextNonOppositeIdx(int start) {

		final List<Imperatives> alternatives = getAlternatives();
		final int len = alternatives.size();

		for (int i = start; i < len; ++i) {
			if (!alternatives.get(i).isOpposite()) {
				return i;
			}
		}

		return -1;
	}

	private void endPrereq(Control control, Code prereqFailed) {
		if (prereqFailed != null && prereqFailed.exists()) {
			prereqFailed.debug("(!) ---------------- Prerequisite failed");
			// prerequisite failed - continue execution
			prereqFailed.go(control.code().tail());
		}
	}

	private void end(Control mainControl, Control control) {
		if (control.isDone()) {
			return;
		}
		if (isClaim()) {
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

	static final class Proposition extends ImperativeSentence {

		Proposition(
				LocationInfo location,
				ImperativeBlock block,
				ImperativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return false;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

	}

	static final class Claim extends ImperativeSentence {

		Claim(
				LocationInfo location,
				ImperativeBlock block,
				ImperativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return true;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

	}

}
