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
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.BinaryOpObj;


public abstract class IntegerBinaryOpObj extends BinaryOpObj<Long, Long> {

	public IntegerBinaryOpObj(
			IntegerObject owner,
			BinaryOperatorInfo operator) {
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


	@Override
	protected final void calculate(
			CodeDirs dirs,
			ObjectOp host,
			ValOp leftVal,
			ValOp rightVal) {

		final Code code = dirs.code();
		final RecOp<Int64op> leftPtr =
			leftVal.rawValue(code.id("left_int_ptr"), code);
		final Int64op left = leftPtr.load(code.id("left"), code);

		final RecOp<Int64op> rightPtr =
			rightVal.rawValue(code.id("right_int_ptr"), code);
		final Int64op right = rightPtr.load(code.id("right"), code);

		leftPtr.store(code, calculate(code, left, right));
	}

	protected abstract Int64op calculate(
			Code code,
			Int64op left,
			Int64op right);

	public static class Add extends IntegerBinaryOpObj {

		public Add(IntegerObject owner) {
			super(owner, BinaryOperatorInfo.ADD);
		}

		@Override
		protected long calculate(long left, long right) {
			return left + right;
		}

		@Override
		protected Int64op calculate(Code code, Int64op left, Int64op right) {
			return left.add(code.id("add"), code, right);
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

		@Override
		protected Int64op calculate(Code code, Int64op left, Int64op right) {
			return left.sub(code.id("sub"), code, right);
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

		@Override
		protected Int64op calculate(Code code, Int64op left, Int64op right) {
			return left.mul(code.id("mul"), code, right);
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

		@Override
		protected Int64op calculate(Code code, Int64op left, Int64op right) {
			return left.div(code.id("div"), code, right);
		}

	}

}
