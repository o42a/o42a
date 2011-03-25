/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class LogicalAction extends Action {

	public LogicalAction(ScopeInfo statement) {
		super(statement);
	}

	@Override
	public Action toInitialLogicalValue() {
		return this;
	}

	@Override
	public Value<?> getValue() {
		switch (getLogicalValue()) {
		case TRUE:
			return Value.voidValue();
		case FALSE:
			return Value.falseValue();
		case RUNTIME:
			return ValueType.VOID.runtimeValue();
		}
		throw new IllegalStateException(
				"Unsupported logical value: " + getLogicalValue());
	}

}
