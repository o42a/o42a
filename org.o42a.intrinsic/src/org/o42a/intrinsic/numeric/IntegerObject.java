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
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.root.Root;


public class IntegerObject extends IntrinsicType {

	public IntegerObject(Root root) {
		super(
				root.toMemberOwner(),
				sourcedDeclaration(root, "integer", "integers/integer.o42a")
				.prototype(),
				ValueType.INTEGER);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				absolutePath(getContext(), "number")
				.target(
						this,
						distributeIn(getScope().getEnclosingContainer()))
				.toTypeRef());
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

}
