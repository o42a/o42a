/*
    Compiler Core
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
package org.o42a.core.artifact.object;

import static org.o42a.core.artifact.object.PropagatedObject.deriveSamples;

import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.DefinitionTarget;


final class OverriddenObject extends PlainObject {

	private final ObjectFieldDecl decl;

	OverriddenObject(ObjectFieldDecl decl) {
		super(decl.getField());
		this.decl = decl;
	}

	@Override
	public String toString() {
		return this.decl != null ? this.decl.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {

		final Ascendants ascendants = new Ascendants(this.decl.getField());

		return this.decl.buildAscendants(
				deriveSamples(this.decl.getField(), ascendants));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.decl.getFieldRegistry().registerMembers(members);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.decl.getEnclosing().define(new DefinitionTarget(
					getScope(),
					getAncestor().getType().getValueType(),
					this.decl.getField().getKey()));
	}

}
