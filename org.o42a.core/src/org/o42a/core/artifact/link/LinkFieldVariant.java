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
package org.o42a.core.artifact.link;

import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.FieldVariant;
import org.o42a.core.st.DefinitionTarget;


final class LinkFieldVariant extends FieldVariant<Link> {

	LinkFieldVariant(
			DeclaredLinkField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	@Override
	protected Definitions define(DefinitionTarget scope) {
		throw new UnsupportedOperationException(
				"An attempt to define a link: " + this);
	}

	@Override
	protected void init() {
		if (!getInitialConditions().isEmpty(getField())) {
			getLogger().prohibitedConditionalDeclaration(this);
			getLinkField().invalid();
		}
	}

	@Override
	protected void declareMembers() {
	}

	private final DeclaredLinkField getLinkField() {
		return (DeclaredLinkField) getField();
	}

}
