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

import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.intrinsic.operator.BinaryOpObj;
import org.o42a.intrinsic.operator.BinaryOperator;


public abstract class NumericEqualsOpObj<L extends Number>
		extends BinaryOpObj<org.o42a.core.value.Void, L> {

	public NumericEqualsOpObj(
			IntrinsicObject owner,
			ValueType<L> leftOperandType) {
		super(
				owner.getContainer(),
				BinaryOperator.EQUALS,
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

	}

	public static class FloatEquals extends NumericEqualsOpObj<Double> {

		public FloatEquals(FloatObject owner) {
			super(owner, ValueType.FLOAT);
		}

		@Override
		protected boolean compare(Double left, Number right) {
			return left.doubleValue() == right.doubleValue();
		}

	}

}
