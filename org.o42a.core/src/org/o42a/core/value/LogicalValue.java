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
package org.o42a.core.value;

import static org.o42a.core.st.DefValue.FALSE_DEF_VALUE;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.DefValue;


public enum LogicalValue {

	TRUE(0, 2) {

		@Override
		public Condition toCondition() {
			return Condition.TRUE;
		}

		@Override
		public DefValue toDefValue() {
			return TRUE_DEF_VALUE;
		}

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.logicalTrue(location, scope);
		}

	},

	RUNTIME(1, 1) {

		@Override
		public Condition toCondition() {
			return Condition.RUNTIME;
		}

		@Override
		public DefValue toDefValue() {
			return RUNTIME_DEF_VALUE;
		}

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.runtimeLogical(location, scope);
		}

	},

	FALSE(2, 0) {

		@Override
		public Condition toCondition() {
			return Condition.FALSE;
		}

		@Override
		public DefValue toDefValue() {
			return FALSE_DEF_VALUE;
		}

		@Override
		public Logical toLogical(LocationInfo location, Scope scope) {
			return Logical.logicalFalse(location, scope);
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

	public abstract Condition toCondition();

	public abstract DefValue toDefValue();

	public abstract Logical toLogical(LocationInfo location, Scope scope);

	public final <T> Value<T> toValue(ValueStruct<?, T> valueStruct) {
		return toCondition().toValue(valueStruct);
	}

}
