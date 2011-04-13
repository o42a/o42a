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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
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

	@Override
	protected final void calculate(
			CodeDirs dirs,
			ObjectOp host,
			ValOp leftVal,
			ValOp rightVal) {

		final Code code = dirs.code();
		final AnyOp leftRec = leftVal.value(code.id("left_ptr"), code);
		final RecOp<Fp64op> leftPtr =
			leftRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op left = leftPtr.load(code.id("left"), code);

		final AnyOp rightRec = rightVal.value(code.id("right_ptr"), code);
		final RecOp<Fp64op> rightPtr =
			rightRec.toFp64(code.id("float_left_ptr"), code);
		final Fp64op right = rightPtr.load(code.id("right"), code);

		leftPtr.store(code, calculate(code, left, right));
	}

	protected abstract Fp64op calculate(Code code, Fp64op left, Fp64op right);

	public static class Add extends FloatBinaryOpObj {

		public Add(FloatObject owner) {
			super(owner, BinaryOperatorInfo.ADD);
		}

		@Override
		protected double calculate(double left, double right) {
			return left + right;
		}

		@Override
		protected Fp64op calculate(Code code, Fp64op left, Fp64op right) {
			return left.add(code.id("add"), code, right);
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

		@Override
		protected Fp64op calculate(Code code, Fp64op left, Fp64op right) {
			return left.sub(code.id("sub"), code, right);
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

		@Override
		protected Fp64op calculate(Code code, Fp64op left, Fp64op right) {
			return left.mul(code.id("mul"), code, right);
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

		@Override
		protected Fp64op calculate(Code code, Fp64op left, Fp64op right) {
			return left.div(code.id("div"), code, right);
		}

	}

}
