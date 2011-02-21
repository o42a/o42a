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
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;


public class ReturnValue extends Action {

	private final Value<?> result;

	public ReturnValue(ScopeInfo statement, Value<?> result) {
		super(statement);
		this.result = result;
	}

	public Value<?> getResult() {
		return this.result;
	}

	@Override
	public LogicalValue getLogicalValue() {
		return getResult().getLogicalValue();
	}

	@Override
	public <P, T> T accept(ActionVisitor<P, T> visitor, P p) {
		return visitor.visitReturnValue(this, p);
	}

	@Override
	public String toString() {
		return "ReturnValue[" + this.result + ']';
	}

}
