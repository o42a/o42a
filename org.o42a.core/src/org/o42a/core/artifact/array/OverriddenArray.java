/*
    Compiler Core
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
package org.o42a.core.artifact.array;

import static org.o42a.core.artifact.array.ArrayTypeRef.arrayTypeRef;

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.Field;


final class OverriddenArray extends Array {

	private final DeclaredArrayField field;

	OverriddenArray(DeclaredArrayField field) {
		super(field, field.getOverridden()[0].getArtifact());
		this.field = field;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.field.validate();
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	@Override
	protected ArrayTypeRef buildTypeRef() {

		final ArrayTypeRef inherited = this.field.inheritedTypeRef();
		final TypeRef declared = this.field.declaredItemTypeRef();

		if (declared != null) {
			if (declared.derivedFrom(inherited.getItemTypeRef())) {
				return arrayTypeRef(declared, inherited.getDimension());
			}
			getLogger().notDerivedFrom(declared, inherited);
			this.field.invalid();
		}

		return inherited;
	}

	@Override
	protected TypeRef buildItemTypeRef() {
		return null;
	}

	@Override
	protected ArrayInitializer buildInitializer() {

		final ArrayInitializer initializer = this.field.declaredInitializer();

		if (initializer != null) {
			return initializer;
		}

		final Field<Array>[] overridden = this.field.getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredInitializer(this.field);
		}

		return overridden[0].getArtifact().getInitializer();
	}

}
