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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.intrinsic.operator.BinaryResult;


abstract class NumbersEqual<P extends Number>
		extends BinaryResult<org.o42a.core.value.Void, P, P> {

	NumbersEqual(
			Container enclosingContainer,
			String name,
			ValueType<P> operandType,
			String sourcePath) {
		super(
				enclosingContainer,
				name,
				ValueType.VOID,
				"what",
				operandType,
				"to",
				operandType,
				sourcePath);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected Void calculate(Scope scope, P left, P right) {
		if (!compare(left, right)) {
			return null;
		}
		return Void.VOID;
	}

	protected abstract boolean compare(P left, Number right);

}
