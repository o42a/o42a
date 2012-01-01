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
package org.o42a.core.artifact.link.impl.decl;

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;

import java.util.List;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.type.TypeRef;


public final class DeclaredLinkField
		extends DeclaredField<Link, LinkFieldVariant> {

	private boolean invalid;
	private FieldDefinition definition;

	public DeclaredLinkField(
			MemberField member,
			ArtifactKind<Link> artifactKind) {
		super(member, artifactKind);
	}

	DeclaredLinkField(MemberField member, Field<Link> propagatedFrom) {
		super(member, propagatedFrom);
	}

	@Override
	protected Link declareArtifact() {
		return new DeclaredLink(getVariant());
	}

	@Override
	protected Link overrideArtifact() {
		return new OverriderLink(getVariant());
	}

	@Override
	protected LinkFieldVariant createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return new LinkFieldVariant(this, declaration, definition);
	}

	@Override
	protected void merge(DeclaredField<Link, LinkFieldVariant> other) {
		getLogger().ambiguousMember(other, getDisplayName());
	}

	@Override
	protected Link propagateArtifact(Field<Link> overridden) {
		return new PropagatedLink(this);
	}

	final boolean isVariable() {
		return getArtifactKind() == ArtifactKind.VARIABLE;
	}

	TypeRef derivedTypeRef() {

		TypeRef typeRef = null;

		for (Field<Link> field : getOverridden()) {

			final TypeRef overriddenTypeRef = field.getArtifact().getTypeRef();

			if (typeRef == null) {
				typeRef = overriddenTypeRef;
			} else {

				final TypeRef commonInheritant =
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

	TargetRef derivedTargetRef() {

		boolean errorReported = false;
		TargetRef result = null;
		Field<Link> lastDefinition = null;

		for (Field<Link> overridden : getOverridden()) {

			final Field<Link> overriddenDefinition =
					overridden.getLastDefinition();

			if (lastDefinition == null) {
				result = overridden.getArtifact().getTargetRef();
				lastDefinition = overriddenDefinition;
				continue;
			}
			if (lastDefinition.derivedFrom(overriddenDefinition)) {
				continue;
			}
			if (overriddenDefinition.derivedFrom(lastDefinition)) {
				result = overridden.getArtifact().getTargetRef();
				lastDefinition = overriddenDefinition;
				continue;
			}
			if (!errorReported) {
				// Report this error at most once.
				getLogger().error(
						"ambiguous_link_target",
						this,
						"It is required to specify a "
						+ (isVariable()
								? "variable initializer" : "link target")
						+ "for field '%s', as it's definition is ambiguous",
						getDisplayName());
				errorReported = true;
				invalid();
			}
		}

		if (result == null) {
			return result;
		}

		return result.upgradeScope(getScope().getEnclosingScope());
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

			final List<LinkFieldVariant> variants = getVariants();

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

	private LinkFieldVariant getVariant() {
		return getVariants().get(0);
	}

}
