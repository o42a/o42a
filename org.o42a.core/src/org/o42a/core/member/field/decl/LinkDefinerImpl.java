/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.member.field.decl;

import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.object.link.TargetRef;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class LinkDefinerImpl implements LinkDefiner {

	private final FieldVariant variant;
	private TargetRef targetRef;
	private Ascendants ascendants;

	LinkDefinerImpl(FieldVariant variant, Ascendants ascendants) {
		this.variant = variant;
		this.ascendants = ascendants;
	}

	@Override
	public DeclaredField getField() {
		return this.variant.getField();
	}

	@Override
	public TypeRef getTypeRef() {
		return this.variant.getField().getDeclaration().getType();
	}

	@Override
	public TargetRef getTargetRef() {
		return this.targetRef;
	}

	@Override
	public void setTargetRef(Ref ref, TypeRef defaultType) {

		final TypeRef explicitTypeRef = explicitTypeRef();

		this.targetRef = ref.toTargetRef(
				explicitTypeRef != null ? explicitTypeRef : defaultType);
		this.variant.getContent().propose(ref).alternative(ref).selfAssign(ref);
	}

	final Ascendants getAscendants() {
		return this.ascendants.setAncestor(
				ancestor(this.targetRef.getTypeRef()));
	}

	private TypeRef explicitTypeRef() {
		return this.variant.getDeclaration().getType();
	}

	private TypeRef ancestor(TypeRef targetType) {
		if (this.ascendants != null) {

			final TypeRef ancestor = this.ascendants.getAncestor();

			if (ancestor != null
					&& ancestor.getValueType().isLink()) {

				final LinkValueType linkType =
						ancestor.getValueType().toLinkType();
				final TypeRef newAncestor = ancestor.setValueStruct(
						linkType.linkStruct(targetType));

				if (!newAncestor.checkDerivedFrom(ancestor)) {
					getField().invalid();
				}

				return newAncestor;
			}
		}

		final LinkValueType linkType =
				this.variant.getDeclaration().getLinkType();
		final FieldDeclaration declaration = this.variant.getDeclaration();

		return linkType.typeRef(
				declaration,
				declaration.getScope(),
				linkType.linkStruct(targetType));
	}

}
