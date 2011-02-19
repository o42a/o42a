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


public abstract class FloatBinaryOpObj extends BinaryOpObj<Double, Double> {

	public FloatBinaryOpObj(FloatObject owner, BinaryOperatorInfo operator) {
		super(
				owner.getContainer(),
				operator,
				owner.getAncestor().toStatic(),
				ValueType.FLOAT,
				ValueType.FLOAT);
	}

	@Override
	protected boolean rightOperandSupported(ValueType<?> valueType) {

		final Class<?> valueClass = valueType.getValueClass();

		return Number.class.isAssignableFrom(valueClass);
	}

	@Override
	protected <R> Double calculate(
			Scope scope,
			Double left,
			ValueType<R> rightType,
			R right) {
		try {
			return calculate(
					left.doubleValue(),
					((Number) right).doubleValue());
		} catch (ArithmeticException e) {
			scope.getLogger().arithmeticError(scope, e.getMessage());
			return null;
		}
	}

	protected abstract double calculate(double left, double right);

	public static class Add extends FloatBinaryOpObj {

		public Add(FloatObject owner) {
			super(owner, BinaryOperatorInfo.ADD);
		}

		@Override
		protected double calculate(double left, double right) {
			return left + right;
		}

	}

	public static class Subtract extends FloatBinaryOpObj {

		public Subtract(FloatObject owner) {
			super(owner, BinaryOperatorInfo.SUBTRACT);
		}

		@Override
		protected double calculate(double left, double right) {
			return left - right;
		}

	}

	public static class Multiply extends FloatBinaryOpObj {

		public Multiply(FloatObject owner) {
			super(owner, BinaryOperatorInfo.MULTIPLY);
		}

		@Override
		protected double calculate(double left, double right) {
			return left * right;
		}

	}

	public static class Divide extends FloatBinaryOpObj {

		public Divide(FloatObject owner) {
			super(owner, BinaryOperatorInfo.DIVIDE);
		}

		@Override
		protected double calculate(double left, double right) {
			return left / right;
		}

	}

}
