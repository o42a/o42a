/*
    Intrinsics
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.common.adapter.UnaryOperatorInfo;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.UnaryOpObj;


public abstract class FloatUnaryOpObj extends UnaryOpObj<Double, Double> {

	public FloatUnaryOpObj(FloatObject owner, UnaryOperatorInfo operator) {
		super(
				owner.getContainer(),
				operator,
				owner.getAncestor().toStatic(),
				ValueType.FLOAT,
				ValueType.FLOAT);
	}

	public static class Plus extends FloatUnaryOpObj {

		public Plus(FloatObject owner) {
			super(owner, UnaryOperatorInfo.PLUS);
		}

		@Override
		protected Double calculate(Double operand) {
			return operand;
		}

	}

	public static final class Minus extends FloatUnaryOpObj {

		public Minus(FloatObject owner) {
			super(owner, UnaryOperatorInfo.MINUS);
		}

		@Override
		protected Double calculate(Double operand) {
			return -operand;
		}

	}

}
