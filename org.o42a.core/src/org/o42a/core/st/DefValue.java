/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st;

import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;


public final class DefValue {

	public static final DefValue TRUE_DEF_VALUE =
			new DefValue(LogicalValue.TRUE, null);
	public static final DefValue FALSE_DEF_VALUE =
			new DefValue(LogicalValue.FALSE, null);
	public static final DefValue RUNTIME_DEF_VALUE =
			new DefValue(LogicalValue.RUNTIME, null);

	public static DefValue defValue(Value<?> value) {
		return new DefValue(value.getKnowledge().toLogicalValue(), value);
	}

	private final LogicalValue logicalValue;
	private final Value<?> value;

	private DefValue(LogicalValue logicalValue, Value<?> value) {
		this.logicalValue = logicalValue;
		this.value = value;
	}

	public final LogicalValue getLogicalValue() {
		return this.logicalValue;
	}

	public final Value<?> getValue() {
		return this.value;
	}

	public final boolean hasValue() {
		return this.value != null;
	}

	@Override
	public String toString() {
		if (this.logicalValue == null) {
			return "NoValue";
		}
		if (this.value != null) {
			return this.value.toString();
		}
		return this.logicalValue.toString();
	}

}
