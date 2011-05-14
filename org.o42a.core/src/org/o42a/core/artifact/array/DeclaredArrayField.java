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

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;

import java.util.List;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.type.TypeRef;


class DeclaredArrayField extends DeclaredField<Array, ArrayFieldVariant> {

	private FieldDefinition definition;
	private boolean invalid;

	DeclaredArrayField(MemberField member) {
		super(member, ArtifactKind.ARRAY);
	}

	private DeclaredArrayField(MemberOwner owner, DeclaredArrayField sample) {
		super(owner, sample);
	}

	@Override
	protected Array declareArtifact() {
		return new DeclaredArray(getVariant());
	}

	@Override
	protected Array overrideArtifact() {
		return new OverriderArray(getVariant());
	}

	@Override
	protected ArrayFieldVariant createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new ArrayFieldVariant(this, declaration, definition);
	}

	@Override
	protected void merge(DeclaredField<Array, ArrayFieldVariant> other) {
		getLogger().ambiguousMember(other, getDisplayName());
	}

	@Override
	protected DeclaredArrayField propagate(MemberOwner owner) {
		return new DeclaredArrayField(owner, this);
	}

	@Override
	protected Array propagateArtifact(Field<Array> overridden) {
		return new PropagatedArray(this);
	}

	TypeRef declaredItemTypeRef() {
		return getDeclaration().getType();
	}

	ArrayTypeRef derivedTypeRef() {

		ArrayTypeRef typeRef = null;

		for (Field<Array> field : getOverridden()) {

			final ArrayTypeRef overriddenTypeRef =
				field.getArtifact().getArrayTypeRef();

			if (typeRef == null) {
				typeRef = overriddenTypeRef;
			} else {

				final ArrayTypeRef commonInheritant =
					typeRef.commonDerivative(overriddenTypeRef);

				if (commonInheritant != null) {
					typeRef = commonInheritant;
				} else {
					getLogger().unexpectedType(
							this,
							overriddenTypeRef,
							typeRef);
					invalid();
				}
			}
		}

		return typeRef != null
		? typeRef.upgradeScope(getEnclosingScope()) : null;
	}

	ArrayInitializer derivedInitializer() {

		final Field<Array>[] overridden = getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredInitializer(this);
		}

		return overridden[0].getArtifact().getInitializer();
	}

	final void invalid() {
		this.invalid = true;
	}

	final boolean validate() {
		return !this.invalid;
	}

	FieldDefinition getDefinition() {
		if (this.definition == null) {

			final List<ArrayFieldVariant> variants = getVariants();

			if (variants.size() != 1) {
				invalid();
				this.definition = invalidDefinition(this, distribute());
			} else {
				this.definition = variants.get(0).getDefinition();
			}
		}

		return this.definition;
	}

	final ArrayFieldVariant getVariant() {
		return getVariants().get(0);
	}

}
