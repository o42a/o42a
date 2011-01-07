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

import static org.o42a.core.artifact.array.ArrayInitializer.invalidArrayInitializer;

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.FieldDefinition;


final class DeclaredArray extends Array {

	private final ArrayFieldDecl decl;

	DeclaredArray(ArrayFieldDecl decl) {
		super(decl.getField());
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
		return null;
	}

	@Override
	protected TypeRef buildItemTypeRef() {
		return this.decl.declaredItemTypeRef();
	}

	@Override
	protected ArrayInitializer buildInitializer() {

		final ArrayInitializer initializer = this.decl.declaredInitializer();

		if (initializer != null) {
			return initializer;
		}

		final FieldDefinition definition = this.decl.getDefinition();

		getLogger().notArray(definition);
		this.decl.invalid();

		return invalidArrayInitializer(definition, definition.distribute());
	}

}
