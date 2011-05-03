/*
    Test Framework
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
package org.o42a.lib.test.rt;

import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;


public class RtFloat extends IntrinsicObject {

	public RtFloat(TestModule module) {
		super(
				module.toMemberOwner(),
				sourcedDeclaration(
						module,
						"rt-float",
						"rt-float.o42a")
				.prototype());
		setValueType(ValueType.FLOAT);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

}
