/*
    Intrinsics
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.core.Container;
import org.o42a.core.artifact.intrinsic.IntrinsicType;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.operator.*;


public class IntegerObject extends IntrinsicType {

	IntegerObject(Container enclosingContainer) {
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
		final UnaryOpObj.Minus<Long> minus = new UnaryOpObj.Minus<Long>(
				this,
				getAncestor().toStatic(),
				ValueType.INTEGER) {
			@Override
			protected Long calculate(Long operand) {
				return -operand;
			}
		};
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

		getFieldRegistry().declareMember(plus.toMember());
		getFieldRegistry().declareMember(minus.toMember());
		getFieldRegistry().declareMember(add.toMember());
		getFieldRegistry().declareMember(subtract.toMember());
		getFieldRegistry().declareMember(multiply.toMember());
		getFieldRegistry().declareMember(divide.toMember());
		getFieldRegistry().declareMember(compare.toMember());
		getFieldRegistry().declareMember(equals.toMember());

		super.declareMembers(members);
	}

}
