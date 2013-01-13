/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.type.TypeRef;


final class KeptValue extends Obj {

	private final KeepValue keepValue;

	KeptValue(KeepValue keepValue) {
		super(keepValue, keepValue.distribute());
		this.keepValue = keepValue;
	}

	@Override
	protected Nesting createNesting() {
		return this.keepValue.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {

		final TypeRef ancestor =
				this.keepValue.getValue().getValueTypeInterface();

		return new Ascendants(this)
		.setAncestor(ancestor)
		.setParameters(
				ancestor.copyParameters()
				.rescope(getScope())
				.toObjectTypeParameters());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new KeptValueDef(this, this.keepValue.getValue())
		.toDefinitions(type().getParameters());
	}

}
