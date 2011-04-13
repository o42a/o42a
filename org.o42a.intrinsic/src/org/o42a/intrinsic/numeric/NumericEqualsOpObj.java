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
import org.o42a.codegen.code.op.*;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.intrinsic.operator.BinaryOpObj;


public abstract class NumericEqualsOpObj<L extends Number>
		extends BinaryOpObj<org.o42a.core.value.Void, L> {

	public NumericEqualsOpObj(
			IntrinsicObject owner,
			ValueType<L> leftOperandType) {
		super(
				owner.getContainer(),
				BinaryOperatorInfo.EQUAL,
				owner.getAncestor().toStatic(),
				ValueType.VOID,
				leftOperandType);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected boolean rightOperandSupported(ValueType<?> valueType) {

		final Class<?> valueClass = valueType.getValueClass();

		return Number.class.isAssignableFrom(valueClass);
	}

	@Override
	protected final <R> Void calculate(
			Scope scope,
			L left,
			ValueType<R> rightType,
			R right) {
		if (!compare(left, (Number) right)) {
			return null;
		}
		return Void.VOID;
	}

	protected abstract boolean compare(L left, Number right);

	public static class IntegerEquals extends NumericEqualsOpObj<Long> {

		public IntegerEquals(IntegerObject owner) {
			super(owner, ValueType.INTEGER);
		}

		@Override
		protected boolean compare(Long left, Number right) {
			return left.longValue() == right.longValue();
		}

		@Override
		protected void calculate(
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

			final BoolOp equals = left.eq(code.id("eq"), code, right);

			dirs.go(code, equals);
			leftVal.storeVoid(code);
		}

	}

	public static class FloatEquals extends NumericEqualsOpObj<Double> {

		public FloatEquals(FloatObject owner) {
			super(owner, ValueType.FLOAT);
		}

		@Override
		protected boolean compare(Double left, Number right) {
			return left.doubleValue() == right.doubleValue();
		}

		@Override
		protected void calculate(
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

			final BoolOp equals = left.eq(code.id("eq"), code, right);

			dirs.go(code, equals);
			leftVal.storeVoid(code);
		}

	}

}
