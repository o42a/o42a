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
package org.o42a.core.artifact.array;

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.Field;


final class PropagatedArray extends Array {

	private final ArrayFieldDecl decl;

	PropagatedArray(ArrayFieldDecl decl) {
		super(
				decl.getField(),
				decl.getField().getOverridden()[0].getArtifact());
		this.decl = decl;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.decl.validate();
	}

	@Override
	public String toString() {
		return this.decl.toString();
	}

	@Override
	protected ArrayTypeRef buildTypeRef() {
		return this.decl.inheritedTypeRef();
	}

	@Override
	protected TypeRef buildItemTypeRef() {
		return null;
	}

	@Override
	protected ArrayInitializer buildInitializer() {

		final Field<Array>[] overridden = this.decl.getField().getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredInitializer(this.decl.getField());
		}

		return overridden[0].getArtifact().getInitializer();
	}

}
