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

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;
import static org.o42a.core.ref.Ref.falseRef;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;


final class DeclaredLinkField extends DeclaredField<Link> {

	private boolean invalid;
	private FieldDefinition definition;

	DeclaredLinkField(MemberField member, ArtifactKind<Link> artifactKind) {
		super(member, artifactKind);
	}

	private DeclaredLinkField(
			Container enclosingContainer,
			DeclaredLinkField sample) {
		super(enclosingContainer, sample);
	}

	@Override
	protected Link declareArtifact() {
		return new DeclaredLink(this);
	}

	@Override
	protected Link overrideArtifact() {
		return new OverriddenLink(this);
	}

	@Override
	protected FieldVariant<Link> createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new LinkFieldVariant(this, declaration, definition);
	}

	@Override
	protected void merge(DeclaredField<Link> other) {
		getLogger().ambiguousMember(other, getDisplayName());
	}

	@Override
	protected DeclaredField<Link> propagate(Scope enclosingScope) {
		return new DeclaredLinkField(enclosingScope.getContainer(), this);
	}

	@Override
	protected Link propagateArtifact(Field<Link> overridden) {
		return new PropagatedLink(this);
	}

	TypeRef inheritedTypeRef() {

		TypeRef typeRef = null;

		for (Field<Link> field : getOverridden()) {

			final TypeRef overriddenTypeRef = field.getArtifact().getTypeRef();

			if (typeRef == null) {
				typeRef = overriddenTypeRef;
			} else {

				final TypeRef commonInheritant =
					typeRef.commonInheritant(overriddenTypeRef);

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

	TargetRef declaredRef() {

		final FieldDefinition definition = getDefinition();

		if (!definition.isValid()) {
			return falseRef(
					this,
					distributeIn(getEnclosingContainer())).toTargetRef();
		}

		final Ref value = definition.getValue();

		if (value == null || !checkInheritable(value)) {
			return falseRef(
					this,
					distributeIn(getEnclosingContainer())).toTargetRef();
		}

		final Resolution resolution = value.getResolution();

		if (resolution.isError()) {
			invalid();
		} else if (!resolution.toArtifact().accessBy(this).checkInstanceUse()) {
			invalid();
		}

		return value.toTargetRef().rescope(getEnclosingScope());
	}

	final void invalid() {
		this.invalid = true;
	}

	final boolean validate(boolean requireDefinition) {
		if (requireDefinition) {
			if (!getDefinition().isValid()) {
				return false;
			}
		}
		getArtifact().resolveAll();
		return !this.invalid;
	}

	FieldDefinition getDefinition() {
		if (this.definition == null) {

			final List<FieldVariant<Link>> variants = getVariants();

			if (variants.size() != 1) {
				if (variants.isEmpty()) {
					getLogger().invalidDeclaration(this);
				} else {
					getLogger().ambiguousMember(this, getDisplayName());
				}
				invalid();
				this.definition = invalidDefinition(
						this,
						distributeIn(getEnclosingContainer()));
			} else {
				this.definition = variants.get(0).getDefinition();
			}
		}

		return this.definition;
	}

	private boolean checkInheritable(Ref value) {

		final Resolution resolution = value.getResolution();

		if (resolution.isError()) {
			return false;
		}
		if (!resolution.toArtifact().getKind().isInheritable()) {
			getLogger().notObjectDeclaration(this);
			invalid();
			return false;
		}

		return true;
	}

}
