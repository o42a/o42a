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
package org.o42a.core.st.action;

import org.o42a.core.ScopeInfo;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;


public class ReturnValue extends Action {

	private final LocalResolver resolver;
	private final Value<?> value;

	public ReturnValue(
			ScopeInfo statement,
			LocalResolver resolver,
			Value<?> value) {
		super(statement);
		this.resolver = resolver;
		this.value = value;
	}

	@Override
	public final Value<?> getValue() {
		return this.value;
	}

	@Override
	public boolean isAbort() {
		return true;
	}

	@Override
	public LogicalValue getLogicalValue() {
		return getValue().getCondition().toLogicalValue();
	}

	@Override
	public Action toInitialLogicalValue() {
		return new ReturnValue(
				this,
				this.resolver,
				getValue().getCondition().toLogicalValue().toValue());
	}

	@Override
	public LoopAction toLoopAction(ImperativeBlock block) {
		return LoopAction.PULL;
	}

	@Override
	public String toString() {
		return "ReturnValue[" + this.value + ']';
	}

}
