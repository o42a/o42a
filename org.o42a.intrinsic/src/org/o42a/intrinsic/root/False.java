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

import static org.o42a.core.def.Def.falseDef;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.core.Scope;
import org.o42a.core.artifact.intrinsic.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public final class False extends IntrinsicObject {

	public False(Root root) {
		super(root, "false");
	}

	@Override
	public String toString() {
		return "false";
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				ValueType.VOID.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions explicitDefinitions() {
		return falseDef(this, distribute()).toDefinitions();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return falseValue();
	}

}
