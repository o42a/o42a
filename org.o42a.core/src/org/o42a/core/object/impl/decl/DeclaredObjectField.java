/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.impl.decl;

import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.field.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.FieldAscendants;


public class DeclaredObjectField
		extends DeclaredField<Obj, ObjectFieldVariant>
		implements FieldAscendants {

	private Ascendants ascendants;
	private Registry memberRegistry;

	public DeclaredObjectField(MemberField member) {
		super(member, ArtifactKind.OBJECT);
	}

	public DeclaredObjectField(MemberField member, Field<Obj> propagatedFrom) {
		super(member, propagatedFrom);
	}

	@Override
	public boolean isLinkAscendants() {
		for (ObjectFieldVariant variant : getVariants()) {
			if (variant.getDefinition().isLink()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Ascendants updateAscendants(Ascendants ascendants) {
		this.ascendants = ascendants;

		for (ObjectFieldVariant variant : getVariants()) {
			this.ascendants = variant.buildAscendants(
					ascendants,
					this.ascendants);
		}

		return this.ascendants;
	}

	@Override
	protected Obj declareArtifact() {
		return new DeclaredObject(this);
	}

	@Override
	protected Obj overrideArtifact() {
		return new OverriderObject(this);
	}

	@Override
	protected void merge(DeclaredField<Obj, ObjectFieldVariant> other) {
		for (FieldVariant<Obj> variant : other.getVariants()) {
			mergeVariant(variant);
		}
	}

	@Override
	protected ObjectFieldVariant createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new ObjectFieldVariant(this, declaration, definition);
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

	Definitions define(Scope scope) {

		Definitions result = null;

		for (ObjectFieldVariant variant : getVariants()) {
			result = variant.define(result, scope);
		}

		return result;
	}

	void updateMembers() {
		for (ObjectFieldVariant variant : getVariants()) {
			variant.declareMembers();
		}
	}

	private final class Registry extends ObjectMemberRegistry {

		Registry() {
			super(newInclusions());
		}

		@Override
		public Obj getOwner() {

			final Obj owner = super.getOwner();

			if (owner != null) {
				return owner;
			}
			return setOwner(getArtifact());
		}

	}

}
