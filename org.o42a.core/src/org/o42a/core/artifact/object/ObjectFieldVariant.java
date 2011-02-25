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
import org.o42a.core.member.field.*;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.sentence.*;


final class ObjectFieldVariant
		extends FieldVariant<Obj>
		implements ObjectDefiner {

	private Block<Declaratives> content;
	private Ascendants implicitAscendants;
	private Ascendants ascendants;

	public ObjectFieldVariant(
			DeclaredObjectField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	public Block<Declaratives> getContent() {
		if (this.content != null) {
			return this.content;
		}

		this.content = new DeclarativeBlock(
				getDefinition(),
				getField(),
				null,
				getObjectField().getMemberRegistry());
		this.content.setConditions(getInitialConditions());

		return this.content;
	}

	@Override
	public final Ascendants getImplicitAscendants() {
		return this.implicitAscendants;
	}

	@Override
	public Ascendants getAscendants() {
		return this.ascendants;
	}

	@Override
	public void setAscendants(Ascendants ascendants) {
		this.ascendants = ascendants;
	}

	@Override
	public void define(BlockBuilder definitions) {
		definitions.buildBlock(getContent());
	}

	@Override
	protected void init() {
	}

	Ascendants buildAscendants(
			Ascendants implicitAscendants,
			Ascendants ascendants) {
		this.implicitAscendants = implicitAscendants;
		this.ascendants = ascendants;
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

	private final DeclaredObjectField getObjectField() {
		return (DeclaredObjectField) getField();
	}

}
