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

import org.o42a.core.Container;
import org.o42a.core.artifact.common.IntrinsicType;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.UnaryOpObj;


public class FloatObject extends IntrinsicType {

	public FloatObject(Container enclosingContainer) {
		super(enclosingContainer, "float", ValueType.FLOAT);
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

		final UnaryOpObj.Plus<Double> plus = new UnaryOpObj.Plus<Double>(
				this,
				getAncestor().toStatic(),
				ValueType.FLOAT);
		final UnaryOpObj.Minus<Double> minus = new UnaryOpObj.Minus<Double>(
				this,
				getAncestor().toStatic(),
				ValueType.FLOAT) {
			@Override
			protected Double calculate(Double operand) {
				return -operand;
			}
		};
		final FloatBinaryOpObj.Add add =
			new FloatBinaryOpObj.Add(this);
		final FloatBinaryOpObj.Subtract subtract =
			new FloatBinaryOpObj.Subtract(this);
		final FloatBinaryOpObj.Multiply multiply =
			new FloatBinaryOpObj.Multiply(this);
		final FloatBinaryOpObj.Divide divide =
			new FloatBinaryOpObj.Divide(this);
		final NumericCompareOpObj.FloatCompare compare =
			new NumericCompareOpObj.FloatCompare(this);
		final NumericEqualsOpObj.FloatEquals equals =
			new NumericEqualsOpObj.FloatEquals(this);
		final FloatByString byString = new FloatByString(this);

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

}
