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
package org.o42a.core.member.field.decl;

import static org.o42a.core.member.Inclusions.noInclusions;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.member.Inclusions;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.member.field.impl.FieldInclusions;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.FieldAscendants;


public final class DeclaredField extends Field implements FieldAscendants {

	private final ArrayList<FieldVariant> variants =
			new ArrayList<FieldVariant>(1);
	private Ascendants ascendants;
	private Registry memberRegistry;
	private boolean invalid;

	public DeclaredField(MemberField member) {
		super(member);
	}

	protected DeclaredField(MemberField member, Field propagatedFrom) {
		super(member);
		setScopeObject(new PropagatedObject(this));
	}

	@Override
	public boolean isLinkAscendants() {
		for (FieldVariant variant : getVariants()) {
			if (variant.getDefinition().isLink()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Ascendants updateAscendants(Ascendants ascendants) {
		this.ascendants = ascendants;

		for (FieldVariant variant : getVariants()) {
			this.ascendants = variant.buildAscendants(
					ascendants,
					this.ascendants);
		}

		return this.ascendants;
	}

	public final List<FieldVariant> getVariants() {
		return this.variants;
	}

	@Override
	public Obj toObject() {
		if (getScopeObject() == null) {
			if (!getKey().isValid()) {

				final Obj falseObject = getContext().getFalse();

				setScopeObject(falseObject);
			} else {
				setScopeObject(new DeclaredObject(this));
			}
		}

		return getScopeObject();
	}

	public final boolean ownsCompilerContext() {

		final Scope enclosingScope = getEnclosingScope();
		final Member enclosingMember = enclosingScope.toMember();

		if (enclosingMember == null) {
			return enclosingScope.getContext() != getContext();
		}

		return !enclosingMember.allContexts().contains(getContext());
	}

	public final Inclusions newInclusions() {
		if (!ownsCompilerContext()) {
			return noInclusions();
		}
		return new FieldInclusions(this);
	}

	protected void mergeVariant(FieldVariant variant) {

		final FieldVariant newVariant =
				variant(variant.getDeclaration(), variant.getDefinition());

		newVariant.setStatement(variant.getStatement());
	}

	@Override
	protected final void merge(Field field) {
		if (!(field instanceof DeclaredField)) {
			getLogger().ambiguousMember(field, getDisplayName());
			return;
		}

		final DeclaredField declaredField = (DeclaredField) field;

		for (FieldVariant variant : declaredField.getVariants()) {
			mergeVariant(variant);
		}
	}

	FieldVariant variant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (!declaration.validateVariantDeclaration(this)) {
			return null;
		}

		final FieldVariant variant =
				new FieldVariant(this, declaration, definition);

		this.variants.add(variant);

		return variant;
	}

	void initDefinitions(Obj object) {

		final Ascendants ascendants =
				new Ascendants(object).declareField(NO_FIELD_ASCENDANTS);

		for (FieldVariant variant : getVariants()) {
			variant.getDefinition().setImplicitAscendants(ascendants);
		}
	}

	ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new Registry();
		}
		return this.memberRegistry;
	}

	Definitions define(Scope scope) {

		Definitions result = null;

		for (FieldVariant variant : getVariants()) {
			result = variant.define(result, scope);
		}

		return result;
	}

	void updateMembers() {
		for (FieldVariant variant : getVariants()) {
			variant.declareMembers();
		}
	}

	final void invalid() {
		this.invalid = true;
	}

	final boolean validate() {
		for (FieldVariant variant : getVariants()) {
			if (!variant.getDefinition().isValid()) {
				return false;
			}
		}
		return !this.invalid;
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
			return setOwner(toObject());
		}

	}

}
