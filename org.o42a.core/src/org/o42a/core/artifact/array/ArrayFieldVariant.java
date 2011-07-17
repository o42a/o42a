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

import org.o42a.core.member.field.*;
import org.o42a.core.ref.type.TypeRef;


final class ArrayFieldVariant
		extends FieldVariant<Array>
		implements ArrayDefiner {

	private ArrayTypeRef typeRef;
	private TypeRef itemTypeRef;
	private ArrayInitializer initializer;

	ArrayFieldVariant(
			DeclaredArrayField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	@Override
	public ArrayTypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public TypeRef getItemTypeRef() {
		return this.itemTypeRef;
	}

	@Override
	public void define(ArrayInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void init() {
	}

	final DeclaredArrayField getArrayField() {
		return (DeclaredArrayField) getField();
	}

	void build(ArrayTypeRef typeRef, TypeRef itemTypeRef) {
		if (getInitialEnv().isConditional()) {
			getLogger().error(
					"prohibiter_conditional_declaration",
					this,
					"Array field '%s' declaration can not be conditional",
					getArrayField().getDisplayName());
			invalid();
		}
		if (getDeclaration().isPrototype()) {
			getLogger().error(
					"prohibited_prototype",
					this,
					"Array field '%s' can not be a prototype",
					getArrayField().getDisplayName());
			invalid();
		}
		this.typeRef = typeRef;
		this.itemTypeRef = itemTypeRef;

		getDefinition().defineArray(this);

		if (this.initializer == null) {
			invalid();
			this.initializer = invalidArrayInitializer(
					getDefinition(),
					getDefinition().distribute());
		}
	}

	final void invalid() {
		getArrayField().invalid();
	}

	final boolean validate() {
		return getArrayField().validate();
	}

	final ArrayInitializer getInitializer() {
		return this.initializer;
	}

}
