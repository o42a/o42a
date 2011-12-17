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
package org.o42a.core.member.field;

import static org.o42a.core.member.Inclusions.noInclusions;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.Inclusions;
import org.o42a.core.member.Member;
import org.o42a.core.member.impl.field.FieldInclusions;


public abstract class DeclaredField<
		A extends Artifact<A>,
		V extends FieldVariant<A>>
				extends Field<A> {

	private final ArtifactKind<A> artifactKind;
	private final ArrayList<V> variants = new ArrayList<V>(1);

	public DeclaredField(MemberField member, ArtifactKind<A> artifactKind) {
		super(member);
		this.artifactKind = artifactKind;
	}

	protected DeclaredField(MemberField member, Field<A> propagatedFrom) {
		super(member);
		this.artifactKind = propagatedFrom.getArtifactKind();
		setScopeArtifact(propagateArtifact(propagatedFrom));
	}

	@Override
	public final ArtifactKind<A> getArtifactKind() {
		return this.artifactKind;
	}

	@SuppressWarnings("unchecked")
	@Override
	public A getArtifact() {
		if (getScopeArtifact() == null) {
			if (!getKey().isValid()) {

				final Artifact<?> falseObject = getContext().getFalse();

				setScopeArtifact((A) falseObject);
			} else if (isOverride()) {
				setScopeArtifact(overrideArtifact());
			} else {
				setScopeArtifact(declareArtifact());
			}
		}

		return getScopeArtifact();
	}

	public final List<V> getVariants() {
		return this.variants;
	}

	public final boolean ownsCompilerContext() {

		final Scope enclosingScope = getEnclosingScope();
		final Member enclosingMember = enclosingScope.toMember();

		if (enclosingMember == null) {
			return enclosingScope.getContext() != getContext();
		}

		return !enclosingMember.getAllContexts().contains(getContext());
	}

	public final Inclusions newInclusions() {
		if (!ownsCompilerContext()) {
			return noInclusions();
		}
		return new FieldInclusions(this);
	}

	protected void mergeVariant(FieldVariant<A> variant) {

		final FieldVariant<A> newVariant =
				variant(variant.getDeclaration(), variant.getDefinition());

		newVariant.setStatement(variant.getStatement());
	}

	protected abstract V createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition);

	protected abstract A declareArtifact();

	protected abstract A overrideArtifact();

	@Override
	protected final void merge(Field<?> field) {
		if (!(field instanceof DeclaredField<?, ?>)) {
			getLogger().ambiguousMember(field, getDisplayName());
			return;
		}
		if (field.getArtifactKind() != getArtifactKind()) {
			getLogger().wrongArtifactKind(
					this,
					field.getArtifactKind(),
					getArtifactKind());
			return;
		}

		@SuppressWarnings("unchecked")
		final DeclaredField<A, V> declaredField =
				(DeclaredField<A, V>) field.toKind(getArtifactKind());

		merge(declaredField);
	}

	protected abstract void merge(DeclaredField<A, V> other);

	protected abstract A propagateArtifact(Field<A> propagatedFrom);

	FieldVariant<A> variant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (!declaration.validateVariantDeclaration(this)) {
			return null;
		}

		final V variant = createVariant(declaration, definition);

		if (variant == null) {
			return null;
		}
		this.variants.add(variant);

		return variant;
	}

}
