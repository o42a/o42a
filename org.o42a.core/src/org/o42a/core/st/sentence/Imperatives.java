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

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.St;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;


public class Imperatives extends Statements<Imperatives> {

	Imperatives(
			LocationInfo location,
			ImperativeSentence sentence,
			boolean opposite) {
		super(location, sentence, opposite);
	}

	@Override
	public final ImperativeSentence getSentence() {
		return (ImperativeSentence) super.getSentence();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	@Override
	public final ImperativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final ImperativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (ImperativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, String name) {
		if (getSentence().isIssue()) {
			getLogger().prohibitedIssueEllipsis(location);
			return;
		}

		final Block<?> block = blockByName(location, name);

		if (block == null) {
			return;
		}
		statement(new EllipsisSt(location, this, name));
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(braces);
	}

	void allocate(LocalBuilder builder, Code code) {
		for (St statement : getStatements()) {
			statement.op(builder).allocate(builder, code);
		}
	}

	void write(Control control, ValOp result) {
		for (St statement : getStatements()) {
			if (!control.reach(statement)) {
				return;
			}

			final StOp op = statement.op(control.getBuilder());

			if (result == null) {
				op.writeLogicalValue(control);
			} else {
				op.writeAssignment(control, result);
			}
		}
	}

	Action initialValue(LocalScope scope) {

		Action result = null;

		for (St statement : getStatements()) {

			final Action action = statement.initialValue(scope);

			if (action.isAbort()) {
				return action;
			}
			if (!action.getLogicalValue().isConstant()) {
				return action;
			}

			result = action;
		}

		if (result != null) {
			return result;
		}

		return new ExecuteCommand(this, LogicalValue.TRUE);
	}

	private Block<?> blockByName(LocationInfo location, String name) {
		if (name == null) {
			return getSentence().getBlock();
		}

		Block<?> block = getSentence().getBlock();

		for (;;) {
			if (name.equals(block.getName())) {
				return block;
			}

			final Statements<?> enclosing = block.getEnclosing();

			if (enclosing == null) {
				getLogger().unresolved(location, name);
				return null;
			}
		}
	}

}
