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

import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.common.intrinsic.IntrinsicType;
import org.o42a.core.Container;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.*;


public class IntegerObject extends IntrinsicType {

	public IntegerObject(Container enclosingContainer) {
		super(enclosingContainer, "integer", ValueType.INTEGER);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				absolutePath(getContext(), "number")
				.target(
						this,
						distributeIn(getScope().getEnclosingContainer()))
				.toTypeRef());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {

		final UnaryOpObj.Plus<Long> plus = new UnaryOpObj.Plus<Long>(
				this,
				getAncestor().toStatic(),
				ValueType.INTEGER);
		final UnaryOpObj.Minus<Long> minus =
			new Minus(this, getAncestor().toStatic(), ValueType.INTEGER);
		final IntegerBinaryOpObj.Add add =
			new IntegerBinaryOpObj.Add(this);
		final IntegerBinaryOpObj.Subtract subtract =
			new IntegerBinaryOpObj.Subtract(this);
		final IntegerBinaryOpObj.Multiply multiply =
			new IntegerBinaryOpObj.Multiply(this);
		final IntegerBinaryOpObj.Divide divide =
			new IntegerBinaryOpObj.Divide(this);
		final NumericCompareOpObj.IntegerCompare compare =
			new NumericCompareOpObj.IntegerCompare(this);
		final NumericEqualsOpObj.IntegerEquals equals =
			new NumericEqualsOpObj.IntegerEquals(this);
		final IntegerByString byString = new IntegerByString(this);

		getMemberRegistry().declareMember(plus.toMember());
		getMemberRegistry().declareMember(minus.toMember());
		getMemberRegistry().declareMember(add.toMember());
		getMemberRegistry().declareMember(subtract.toMember());
		getMemberRegistry().declareMember(multiply.toMember());
		getMemberRegistry().declareMember(divide.toMember());
		getMemberRegistry().declareMember(compare.toMember());
		getMemberRegistry().declareMember(equals.toMember());
		getMemberRegistry().declareMember(byString.toMember());

		super.declareMembers(members);
	}

	private static final class Minus extends UnaryOpObj.Minus<Long> {

		Minus(
				Container enclosingContainer,
				StaticTypeRef declaredIn,
				ValueType<Long> operandType) {
			super(enclosingContainer, declaredIn, operandType);
		}

		@Override
		protected Long calculate(Long operand) {
			return -operand;
		}

	}

}
