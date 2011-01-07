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

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;


final class LinkFieldDecl extends FieldDecl<Link> {

	private final ArtifactKind<Link> kind;
	private boolean invalid;
	private FieldDefinition definition;

	LinkFieldDecl(DeclaredField<Link> field, ArtifactKind<Link> kind) {
		super(field);
		this.kind = kind;
	}

	public final ArtifactKind<Link> getKind() {
		return this.kind;
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
	protected Link propagateArtifact() {
		return new PropagatedLink(this);
	}

	@Override
	protected FieldVariantDecl<Link> variantDecl(FieldVariant<Link> variant) {
		if (variant.getEnclosing().getSentence().isConditional()) {
			getLogger().prohibitedConditionalDeclaration(variant);
		}
		return new LinkFieldVariantDecl(this, variant);
	}

	@Override
	protected void merge(FieldDecl<?> decl) {
		getLogger().ambiguousField(
				decl.getField(),
				getField().getDisplayName());
	}

	TypeRef inheritedTypeRef() {

		TypeRef typeRef = null;

		for (Field<Link> field : getField().getOverridden()) {

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
							getField(),
							overriddenTypeRef,
							typeRef);
					invalid();
				}
			}
		}

		return typeRef != null
		? typeRef.upgradeScope(getField().getEnclosingScope()) : null;
	}

	TargetRef declaredRef() {

		final FieldDefinition definition = getDefinition();

		if (!definition.isValid()) {
			return falseRef(
					getField(),
					getField().distributeIn(
							getField().getEnclosingContainer())).toTargetRef();
		}

		final Ref value = definition.getValue();

		if (value == null || !checkInheritable(value)) {
			return falseRef(
					getField(),
					getField().distributeIn(
							getField().getEnclosingContainer())).toTargetRef();
		}

		final Resolution resolution = value.getResolution();

		if (resolution.isError()) {
			invalid();
		} else if (!resolution.toArtifact().accessBy(getField())
				.checkInstanceUse()) {
			invalid();
		}

		return value.toTargetRef().rescope(getField().getEnclosingScope());
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
		getField().getArtifact().resolveAll();
		return !this.invalid;
	}

	FieldDefinition getDefinition() {
		if (this.definition == null) {

			final List<FieldVariant<Link>> variants = getField().getVariants();

			if (variants.size() != 1) {
				if (variants.isEmpty()) {
					getLogger().invalidDeclaration(getField());
				} else {
					getLogger().ambiguousField(
							getField(),
							getField().getDisplayName());
				}
				invalid();
				this.definition = invalidDefinition(
						getField(),
						getField().distributeIn(
								getField().getEnclosingContainer()));
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
			getLogger().notObjectDeclaration(getField());
			invalid();
			return false;
		}

		return true;
	}

}
