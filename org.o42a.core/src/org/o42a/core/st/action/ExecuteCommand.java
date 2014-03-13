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
package org.o42a.core.st.action;

import org.o42a.core.ScopeInfo;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public class ExecuteCommand extends Action {

	private final Condition condition;

	public ExecuteCommand(ScopeInfo statement, Condition condition) {
		super(statement);
		this.condition = condition;
	}

	@Override
	public boolean isAbort() {
		return false;
	}

	@Override
	public Condition getCondition() {
		return this.condition;
	}

	@Override
	public Value<?> getValue() {
		return null;
	}

	@Override
	public LoopAction toLoopAction(Block block) {
		if (getCondition().isTrue()) {
			return LoopAction.CONTINUE;
		}
		return LoopAction.EXIT;
	}

	@Override
	public DefValue toDefValue() {
		return getCondition().toDefValue();
	}

}
