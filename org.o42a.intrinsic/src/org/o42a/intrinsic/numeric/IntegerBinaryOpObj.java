/*
    Intrinsics
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
package org.o42a.intrinsic.numeric;

import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.Scope;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.BinaryOpObj;


public abstract class IntegerBinaryOpObj extends BinaryOpObj<Long, Long> {

	public IntegerBinaryOpObj(IntegerObject owner, BinaryOperatorInfo operator) {
		super(
				owner.getContainer(),
				operator,
				owner.getAncestor().toStatic(),
				ValueType.INTEGER,
				ValueType.INTEGER);
	}

	@Override
	protected boolean rightOperandSupported(ValueType<?> valueType) {

		final Class<?> valueClass = valueType.getValueClass();

		return Number.class.isAssignableFrom(valueClass);
	}

	@Override
	protected <R> Long calculate(
			Scope scope,
			Long left,
			ValueType<R> rightType,
			R right) {
		try {
			return calculate(left.longValue(), ((Number) right).longValue());
		} catch (ArithmeticException e) {
			scope.getLogger().arithmeticError(scope, e.getMessage());
			return null;
		}
	}

	protected abstract long calculate(long left, long right);

	public static class Add extends IntegerBinaryOpObj {

		public Add(IntegerObject owner) {
			super(owner, BinaryOperatorInfo.ADD);
		}

		@Override
		protected long calculate(long left, long right) {
			return left + right;
		}

	}

	public static class Subtract extends IntegerBinaryOpObj {

		public Subtract(IntegerObject owner) {
			super(owner, BinaryOperatorInfo.SUBTRACT);
		}

		@Override
		protected long calculate(long left, long right) {
			return left - right;
		}

	}

	public static class Multiply extends IntegerBinaryOpObj {

		public Multiply(IntegerObject owner) {
			super(owner, BinaryOperatorInfo.MULTIPLY);
		}

		@Override
		protected long calculate(long left, long right) {
			return left * right;
		}

	}

	public static class Divide extends IntegerBinaryOpObj {

		public Divide(IntegerObject owner) {
			super(owner, BinaryOperatorInfo.DIVIDE);
		}

		@Override
		protected long calculate(long left, long right) {
			return left / right;
		}

	}

}
