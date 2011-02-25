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

import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.FieldVariant;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;


final class LinkFieldVariant extends FieldVariant<Link>
		implements FieldDefinition.LinkDefiner {

	private TypeRef typeRef;
	private TargetRef defaultTargetRef;
	private TargetRef targetRef;

	LinkFieldVariant(
			DeclaredLinkField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(field, declaration, definition);
	}

	@Override
	public TypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public TargetRef getDefaultTargetRef() {
		return this.defaultTargetRef;
	}

	@Override
	public TargetRef getTargetRef() {
		return this.targetRef;
	}

	@Override
	public void setTargetRef(TargetRef targetRef) {
		this.targetRef = targetRef;
	}

	@Override
	protected void init() {
		if (!getInitialConditions().isEmpty(getField())) {
			getLogger().prohibitedConditionalDeclaration(this);
			invalid();
		}
	}

	final TargetRef build(TypeRef typeRef, TargetRef defaultTargetRef) {
		this.typeRef = typeRef;
		this.targetRef = this.defaultTargetRef = defaultTargetRef;

		getDefinition().defineLink(this);

		if (typeRef != null) {

			final TypeRelation relation =
				typeRef.relationTo(this.typeRef);

			if (!relation.isAscendant()) {
				if (!relation.isError()) {
					getLogger().notDerivedFrom(this.typeRef, this.targetRef);
				}
				invalid();
			}
		}

		return this.targetRef;
	}

	final DeclaredLinkField getLinkField() {
		return (DeclaredLinkField) getField();
	}

	final void invalid() {
		getLinkField().invalid();
	}

}
