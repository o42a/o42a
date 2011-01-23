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
package org.o42a.core.artifact.object;

import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.FieldVariant;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.sentence.*;


final class ObjectFieldVariant extends FieldVariant<Obj> {

	private Block<Declaratives> content;

	public ObjectFieldVariant(
			DeclaredObjectField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	public Block<Declaratives> getContent() {
		if (this.content == null) {
			buildContent();
		}
		return this.content;
	}

	@Override
	protected void init() {
		getContent();
	}

	@Override
	protected void declareMembers() {
		getContent().executeInstructions();
	}

	@Override
	protected Definitions define(DefinitionTarget target) {
		return getContent().define(target);
	}

	private void buildContent() {

		final FieldDefinition definition = getDefinition();

		this.content = new DeclarativeBlock(
				definition,
				getField(),
				null,
				getObjectField().getMemberRegistry());
		this.content.setConditions(getInitialConditions());

		final BlockBuilder declarations = definition.getDeclarations();

		if (declarations != null) {
			declarations.buildBlock(this.content);
		} else {

			final Ref value = definition.getValue();

			if (value != null) {
				this.content.propose(value).alternative(value).assign(value);
			}
		}
	}

	private final DeclaredObjectField getObjectField() {
		return (DeclaredObjectField) getField();
	}

}
