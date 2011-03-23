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

import static org.o42a.core.ir.op.Val.FALSE_VAL;
import static org.o42a.core.ir.op.Val.UNKNOWN_VAL;

import org.o42a.codegen.Generator;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.ir.op.Val;


final class DefiniteValue<T> extends Value<T> {

	private final T value;

	DefiniteValue(ValueType<T> valueType, T value) {
		super(valueType);
		this.value = value;
	}

	@Override
	public LogicalValue getLogicalValue() {
		return LogicalValue.TRUE;
	}

	@Override
	public T getDefiniteValue() {
		return this.value;
	}

	@Override
	public Val val(Generator generator) {
		if (!getLogicalValue().isConstant()) {
			return UNKNOWN_VAL;
		}
		if (getLogicalValue().isFalse()) {
			return FALSE_VAL;
		}

		final T definiteValue = getDefiniteValue();

		if (definiteValue == null) {
			return UNKNOWN_VAL;
		}

		return getValueType().val(generator, definiteValue);
	}

	@Override
	public String toString() {
		return valueString(getValueType(), this.value);
	}

	private static <T> String valueString(ValueType<T> valueType, T value) {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(valueType).append(") ");
		out.append(valueType.valueString(value));

		return out.toString();
	}

	static final class DefiniteObject<T> extends Result {

		private final T value;

		DefiniteObject(
				LocationInfo location,
				Distributor enclosing,
				ValueType<T> valueType,
				T value) {
			super(location, enclosing, valueType);
			setValueType(valueType);
			this.value = value;
		}

		@Override
		public boolean isRuntime() {
			super.isRuntime();
			return false;
		}

		@Override
		public String toString() {

			@SuppressWarnings("unchecked")
			final ValueType<T> valueType = (ValueType<T>) getValueType();

			return valueString(valueType, this.value);
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {

			@SuppressWarnings("unchecked")
			final ValueType<T> valueType = (ValueType<T>) getValueType();

			return valueType.definiteValue(this.value);
		}

	}

}
