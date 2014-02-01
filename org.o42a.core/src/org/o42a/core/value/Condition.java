/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.st.DefValue;


public enum Condition {

	TRUE() {

		@Override
		public DefValue toDefValue() {
			return TRUE_DEF_VALUE;
		}

		@Override
		public <T> Value<T> toValue(TypeParameters<T> typeParameters) {

			final ValueType<T> valueType = typeParameters.getValueType();

			assert valueType.isVoid() :
				"Can not construct a non-void TRUE";

			return valueType.cast(
					ValueType.VOID.cast(typeParameters)
					.compilerValue(Void.VOID));
		}

	},

	RUNTIME() {

		@Override
		public DefValue toDefValue() {
			return RUNTIME_DEF_VALUE;
		}

		@Override
		public <T> Value<T> toValue(TypeParameters<T> typeParameters) {
			return typeParameters.runtimeValue();
		}

	},

	FALSE() {

		@Override
		public DefValue toDefValue() {
			return FALSE_DEF_VALUE;
		}

		@Override
		public <T> Value<T> toValue(TypeParameters<T> typeParameters) {
			return typeParameters.falseValue();
		}

	};

	public final boolean isConstant() {
		return this != RUNTIME;
	}

	public final boolean isTrue() {
		return this == TRUE;
	}

	public final boolean isFalse() {
		return this == FALSE;
	}

	public Condition negate() {
		switch (this) {
		case FALSE:
			return Condition.TRUE;
		case TRUE:
			return Condition.FALSE;
		case RUNTIME:
		default:
			return Condition.RUNTIME;
		}
	}

	public abstract DefValue toDefValue();

	public abstract <T> Value<T> toValue(TypeParameters<T> typeParameters);

}
