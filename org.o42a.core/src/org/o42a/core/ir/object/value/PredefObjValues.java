/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;


final class PredefObjValues {

	static PredefObjValues predefObjValues(Generator generator) {

		final PredefObjValues existing =
				generator.getFeature(PredefObjValues.class);

		if (existing != null) {
			return existing;
		}

		final PredefObjValues values = new PredefObjValues(generator);

		generator.setFeature(PredefObjValues.class, values);

		return values;
	}

	private final Generator generator;
	private final HashMap<PredefKey, FuncPtr<ObjectValueFn>> valueFunctions =
			new HashMap<>();

	private PredefObjValues(Generator generator) {
		this.generator = generator;
	}

	final FuncPtr<ObjectValueFn> valueFunction(
			CompilerContext context,
			PredefObjValue value,
			ValueType<?> valueType) {

		final PredefKey key = prefefKey(value, valueType);
		final FuncPtr<ObjectValueFn> cached = this.valueFunctions.get(key);

		if (cached != null) {
			return cached;
		}

		final FuncPtr<ObjectValueFn> function = value.createValueFunction(
				context,
				this.generator,
				valueType);

		this.valueFunctions.put(key, function);

		return function;
	}

	private static PredefKey prefefKey(
			PredefObjValue value,
			ValueType<?> valueType) {

		final ValueType<?> type =
				value.isTypeAware() ? valueType : ValueType.VOID;

		return new PredefKey(value, type);
	}

	private static final class PredefKey {

		private final PredefObjValue value;
		private final ValueType<?> valueType;

		PredefKey(PredefObjValue value, ValueType<?> valueType) {
			this.value = value;
			this.valueType = valueType;
		}

		@Override
		public int hashCode() {

			final int prime = 31;
			int result = 1;

			result = prime * result + this.value.hashCode();
			result = prime * result + this.valueType.hashCode();

			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final PredefKey other = (PredefKey) obj;

			if (!this.value.equals(other.value)) {
				return false;
			}
			if (!this.valueType.equals(other.valueType)) {
				return false;
			}

			return true;
		}

	}

}
