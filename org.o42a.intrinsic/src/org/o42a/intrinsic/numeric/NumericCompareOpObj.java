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
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.BinaryOpObj;


public abstract class NumericCompareOpObj<L extends Number>
		extends BinaryOpObj<Long, L> {

	public NumericCompareOpObj(
			IntrinsicObject owner,
			ValueType<L> leftOperandType) {
		super(
				owner.getContainer(),
				BinaryOperatorInfo.COMPARE,
				owner.getAncestor().toStatic(),
				ValueType.INTEGER,
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
	protected <R> Long calculate(
			Scope scope,
			L left,
			ValueType<R> rightType,
			R right) {
		return compare(left, (Number) right);
	}

	protected abstract long compare(L left, Number right);

	public static class IntegerCompare extends NumericCompareOpObj<Long> {

		public IntegerCompare(IntegerObject owner) {
			super(owner, ValueType.INTEGER);
		}

		@Override
		protected long compare(Long left, Number right) {
			return left.compareTo(right.longValue());
		}

	}

	public static class FloatCompare extends NumericCompareOpObj<Double> {

		public FloatCompare(FloatObject owner) {
			super(owner, ValueType.FLOAT);
		}

		@Override
		protected long compare(Double left, Number right) {
			return left.compareTo(right.doubleValue());
		}

	}

}
