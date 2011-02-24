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


final class ObjectFieldVariant
		extends FieldVariant<Obj>
		implements FieldDefinition.ObjectDefiner {

	private Block<Declaratives> content;
	private Ascendants implicitAscendants;
	private Ascendants ascendants;
	private BlockBuilder definitions;

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
	public final Ascendants getImplicitAscendants() {
		return this.implicitAscendants;
	}

	@Override
	public void setAscendants(Ascendants ascendants) {
		this.ascendants = ascendants;
	}

	@Override
	public void setDefinitions(BlockBuilder definitions) {
		this.definitions = definitions;
	}

	@Override
	protected void init() {
	}

	Ascendants buildAscendants(Ascendants implicitAscendants) {
		this.implicitAscendants = implicitAscendants;
		this.ascendants = implicitAscendants;
		getDefinition().defineObject(this);
		return this.ascendants;
	}

	void declareMembers() {
		getContent().executeInstructions();
	}

	Definitions define(Definitions definitions, DefinitionTarget target) {

		final Definitions variantDefinitions = getContent().define(target);

		if (variantDefinitions == null) {
			return definitions;
		}
		if (definitions == null) {
			return variantDefinitions;
		}

		return definitions.refine(variantDefinitions);
	}

	private void buildContent() {
		this.content = new DeclarativeBlock(
				getDefinition(),
				getField(),
				null,
				getObjectField().getMemberRegistry());
		this.content.setConditions(getInitialConditions());

		if (this.definitions != null) {
			this.definitions.buildBlock(this.content);
		} else {
			// TODO remove definition by value

			final Ref value = getDefinition().getValue();

			if (value != null) {
				this.content.propose(value).alternative(value).assign(value);
			}
		}
	}

	private final DeclaredObjectField getObjectField() {
		return (DeclaredObjectField) getField();
	}

}
