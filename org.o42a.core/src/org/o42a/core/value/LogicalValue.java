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
package org.o42a.core.value;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;


public enum LogicalValue {

	TRUE(0, 2) {

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.logicalTrue(location, scope);
		}

		@Override
		public Value<Void> toValue() {
			return Value.voidValue();
		}

	},

	RUNTIME(1, 1) {

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.runtimeLogical(location, scope);
		}

		@Override
		public Value<Void> toValue() {
			return ValueType.VOID.runtimeValue();
		}

	},

	FALSE(2, 0) {

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.logicalFalse(location, scope);
		}

		@Override
		public Value<Void> toValue() {
			return Value.falseValue();
		}

	};

	private final int orPriority;
	private final int andPriority;

	LogicalValue(int orPriority, int andPriority) {
		this.orPriority = orPriority;
		this.andPriority = andPriority;
	}

	public final boolean isConstant() {
		return this != RUNTIME;
	}

	public final boolean isTrue() {
		return this == TRUE;
	}

	public final boolean isFalse() {
		return this == FALSE;
	}

	public LogicalValue negate() {
		switch (this) {
		case FALSE:
			return LogicalValue.TRUE;
		case TRUE:
			return LogicalValue.FALSE;
		case RUNTIME:
		default:
			return LogicalValue.RUNTIME;
		}
	}

	public LogicalValue or(LogicalValue other) {
		if (other == null) {
			return this;
		}
		if (other.orPriority < this.orPriority) {
			return other;
		}
		return this;
	}

	public LogicalValue and(LogicalValue other) {
		if (other == null) {
			return this;
		}
		if (other.andPriority < this.andPriority) {
			return other;
		}
		return this;
	}

	public abstract Logical toLogical(LocationInfo location, Scope scope);

	public abstract Value<Void> toValue();

}
