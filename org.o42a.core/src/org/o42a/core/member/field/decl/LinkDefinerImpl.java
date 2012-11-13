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
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.TargetRef;


final class LinkDefinerImpl implements LinkDefiner {

	private final DeclaredField field;
	private TargetRef targetRef;
	private Ascendants ascendants;

	LinkDefinerImpl(DeclaredField field, Ascendants implicitAscendants) {
		this.field = field;
		this.ascendants = implicitAscendants;
	}

	@Override
	public final DeclaredField getField() {
		return this.field;
	}

	@Override
	public void setTargetRef(Ref ref, TypeRef defaultType) {

		final TypeRef explicitTypeRef = explicitTypeRef();

		this.targetRef = ref.toTargetRef(
				explicitTypeRef != null ? explicitTypeRef : defaultType);
		this.field.getContent().propose(ref).alternative(ref).selfAssign(ref);
	}

	@Override
	public void define(BlockBuilder definitions) {
		definitions.buildBlock(getField().getContent());
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return "LinkDefiner[" + this.field + ']';
	}

	final Ascendants getAscendants() {

		final TypeRef targetType;

		if (this.targetRef == null) {

			final TypeRef explicitTypeRef = explicitTypeRef();

			if (explicitTypeRef == null) {
				return this.ascendants;
			}

			targetType = explicitTypeRef;
		} else {
			targetType = this.targetRef.getTypeRef();
		}

		final FieldDeclaration declaration = getField().getDeclaration();
		final LinkValueType linkType = declaration.getLinkType();

		if (!this.ascendants.isEmpty()) {
			return this.ascendants.setTypeParameters(
					linkType.typeParameters(targetType));
		}

		return this.ascendants.setAncestor(
				linkType.typeRef(
						declaration,
						declaration.getScope(),
						linkType.linkStruct(targetType)));
	}

	private TypeRef explicitTypeRef() {
		return this.field.getDeclaration().getType();
	}

}
