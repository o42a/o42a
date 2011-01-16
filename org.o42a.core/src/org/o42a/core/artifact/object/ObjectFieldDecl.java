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

import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.*;


class ObjectFieldDecl extends FieldDecl<Obj> {

	private Registry memberRegistry;

	ObjectFieldDecl(DeclaredField<Obj> field) {
		super(field);
	}

	@Override
	protected Obj declareArtifact() {

		final Ascendants ascendants =
			buildAscendants(new Ascendants(getField()));

		if (ascendants.getAncestor() != null) {
			return new DeclaredCall(this, ascendants);
		}

		return new DeclaredObject(this);
	}

	@Override
	protected Obj overrideArtifact() {
		return new OverriddenObject(this);
	}

	@Override
	protected Obj propagateArtifact() {
		return new PropagatedObject(getField());
	}

	@Override
	protected FieldVariantDecl<Obj> variantDecl(FieldVariant<Obj> variant) {
		return new ObjectFieldVariantDecl(this, variant);
	}

	@Override
	protected void merge(FieldDecl<?> decl) {
		if (decl.getField().getArtifact().getKind() != ArtifactKind.OBJECT) {
			getField().getLogger().notObjectDeclaration(decl.getField());
			return;
		}

		@SuppressWarnings("unchecked")
		final DeclaredField<Obj> other = (DeclaredField<Obj>) decl.getField();
		final DeclaredField<Obj> field = getField();

		for (FieldVariant<Obj> variant : other.getVariants()) {
			if (variant.getDeclaration().validateVariantDeclaration(field)) {
				addVariant(variant);
			}
		}
	}

	ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	Ascendants buildAscendants(Ascendants ascendants) {
		for (FieldVariant<Obj> variant : getField().getVariants()) {
			updateAscendants(ascendants, variant);
		}
		return ascendants;
	}

	private void updateAscendants(
			Ascendants ascendants,
			FieldVariant<Obj> variant) {

		final Scope scope = getField().getEnclosingScope();

		variant.getDefinition().getAscendants().updateAscendants(
				scope,
				ascendants);

		final AdapterId adapterId =
			getField().toMember().getId().getAdapterId();

		if (adapterId != null) {
			ascendants.addExplicitSample(adapterId.adapterType(scope));
		}
	}

	private final class Registry extends OwnerFieldRegistry {

		@Override
		protected Obj findOwner() {
			return getField().getArtifact();
		}

	}

}
