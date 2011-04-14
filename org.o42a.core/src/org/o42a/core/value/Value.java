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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.op.Val;


public abstract class Value<T> {

	public static final Value<java.lang.Void> NO_VALUE = new NoValue();

	public static final Value<Void> voidValue() {
		return ValueType.VOID.definiteValue(Void.VOID);
	}

	public static final Value<Void> falseValue() {
		return ValueType.VOID.falseValue();
	}

	public static final Value<Void> unknownValue() {
		return ValueType.VOID.unknownValue();
	}

	private final ValueType<T> valueType;

	Value(ValueType<T> valueType) {
		this.valueType = valueType;
	}

	public final ValueType<T> getValueType() {
		return this.valueType;
	}

	public final boolean isFalse() {
		return getLogicalValue().isFalse();
	}

	public boolean isUnknown() {
		return false;
	}

	public final boolean isDefinite() {
		return getLogicalValue().isConstant();
	}

	public final boolean isVoid() {
		return getValueType() == ValueType.VOID;
	}

	public abstract LogicalValue getLogicalValue();

	public abstract T getDefiniteValue();

	public abstract Val val(Generator generator);

	public Value<T> require(LogicalValue requirement) {

		final LogicalValue logicalValue = getLogicalValue();
		final LogicalValue newLogicalValue = logicalValue.and(requirement);

		if (logicalValue == newLogicalValue) {
			return this;
		}

		switch (newLogicalValue) {
		case FALSE:
			return getValueType().falseValue();
		case TRUE:
			return getValueType().definiteValue(getDefiniteValue());
		case RUNTIME:
		}

		return getValueType().runtimeValue();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(this.valueType).append(") ");

		final LogicalValue logicalValue = getLogicalValue();

		if (logicalValue.isTrue()) {
			out.append(getDefiniteValue());
		} else {
			out.append(logicalValue);
		}

		return out.toString();
	}

	private static final class NoValue extends Value<java.lang.Void> {

		NoValue() {
			super(ValueType.NONE);
		}

		@Override
		public LogicalValue getLogicalValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.lang.Void getDefiniteValue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Val val(Generator generator) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "No value";
		}

	}

}
