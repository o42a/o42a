/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.value.Value.voidValue;


public enum Condition {

	TRUE(LogicalValue.TRUE) {

		@Override
		public <T> Value<T> toValue(ValueStruct<?, T> valueStruct) {
			assert valueStruct.isVoid() :
				"Can not construct a non-void TRUE";
			return valueStruct.cast(voidValue());
		}

	},

	RUNTIME(LogicalValue.RUNTIME) {

		@Override
		public <T> Value<T> toValue(ValueStruct<?, T> valueStruct) {
			return valueStruct.runtimeValue();
		}

	},

	FALSE(LogicalValue.FALSE) {

		@Override
		public <T> Value<T> toValue(ValueStruct<?, T> valueStruct) {
			return valueStruct.falseValue();
		}

	};

	private final LogicalValue logicalValue;

	Condition(LogicalValue logicalValue) {
		this.logicalValue = logicalValue;
	}

	public final boolean isConstant() {
		return toLogicalValue().isConstant();
	}

	public final boolean isTrue() {
		return toLogicalValue().isTrue();
	}

	public final boolean isFalse() {
		return toLogicalValue().isFalse();
	}

	public final LogicalValue toLogicalValue() {
		return this.logicalValue;
	}

	public abstract <T> Value<T> toValue(ValueStruct<?, T> valueStruct);

}
