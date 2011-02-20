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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.*;


class DeclaredObjectField extends DeclaredField<Obj> {

	private Registry memberRegistry;

	DeclaredObjectField(MemberField member) {
		super(member, ArtifactKind.OBJECT);
	}

	private DeclaredObjectField(
			Container enclosingContainer,
			DeclaredField<Obj> sample) {
		super(enclosingContainer, sample);
	}

	@Override
	protected Obj declareArtifact() {

		final Ascendants ascendants = buildAscendants(new Ascendants(this));

		return new DeclaredObject(this, ascendants);
	}

	@Override
	protected Obj overrideArtifact() {
		return new OverriddenObject(this);
	}

	@Override
	protected void merge(DeclaredField<Obj> other) {
		for (FieldVariant<Obj> variant : other.getVariants()) {
			mergeVariant(variant);
		}
	}

	@Override
	protected FieldVariant<Obj> createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new ObjectFieldVariant(this, declaration, definition);
	}

	@Override
	protected DeclaredField<Obj> propagate(Scope enclosingScope) {
		return new DeclaredObjectField(enclosingScope.getContainer(), this);
	}

	@Override
	protected Obj propagateArtifact(Field<Obj> overridden) {
		return new PropagatedObject(this);
	}

	ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	Ascendants buildAscendants(Ascendants ascendants) {
		for (FieldVariant<Obj> variant : getVariants()) {
			updateAscendants(ascendants, variant);
		}
		return ascendants;
	}

	private void updateAscendants(
			Ascendants ascendants,
			FieldVariant<Obj> variant) {

		final Scope scope = getEnclosingScope();

		ascendants = variant.getDefinition().getAscendants().updateAscendants(
				scope,
				ascendants);

		final AdapterId adapterId = toMember().getId().getAdapterId();

		if (adapterId != null) {
			ascendants =
				ascendants.addExplicitSample(adapterId.adapterType(scope));
		}
	}

	private final class Registry extends OwnerMemberRegistry {

		@Override
		protected Obj findOwner() {
			return getArtifact();
		}

	}

}
