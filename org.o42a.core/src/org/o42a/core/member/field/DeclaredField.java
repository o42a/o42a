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

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.DefinitionTarget;


public abstract class DeclaredField<A extends Artifact<A>> extends Field<A> {

	private final ArtifactKind<A> artifactKind;
	private final ArrayList<FieldVariant<A>> variants =
		new ArrayList<FieldVariant<A>>();

	public DeclaredField(MemberField member, ArtifactKind<A> artifactKind) {
		super(member);
		this.artifactKind = artifactKind;
	}

	protected DeclaredField(
			Container enclosingContainer,
			DeclaredField<A> sample) {
		super(enclosingContainer, sample);
		this.artifactKind = sample.artifactKind;
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
			for (FieldVariant<A> variant : getVariants()) {
				variant.init();
			}
		}

		return getScopeArtifact();
	}

	public final List<FieldVariant<A>> getVariants() {
		return this.variants;
	}

	public Definitions define(DefinitionTarget target) {

		Definitions result = null;

		for (FieldVariant<A> variant : getVariants()) {

			final Definitions definition = variant.define(target);

			if (definition == null) {
				continue;
			}
			if (result == null) {
				result = definition;
			} else {
				result = result.refine(definition);
			}
		}

		return result;
	}

	public void declareMembers() {
		for (FieldVariant<A> variant : getVariants()) {
			variant.declareMembers();
		}
	}

	protected void mergeVariant(FieldVariant<A> variant) {

		final FieldVariant<A> newVariant =
			variant(variant.getDeclaration(), variant.getDefinition());

		newVariant.setStatement(variant.getStatement());
	}

	protected abstract FieldVariant<A> createVariant(
			FieldDeclaration declaration,
			FieldDefinition definition);

	protected abstract A declareArtifact();

	protected abstract A overrideArtifact();

	@Override
	protected final void merge(Field<?> field) {
		if (!(field instanceof DeclaredField<?>)) {
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

		final DeclaredField<A> declaredField =
			(DeclaredField<A>) field.toKind(getArtifactKind());

		merge(declaredField);
	}

	protected abstract void merge(DeclaredField<A> other);

	@Override
	protected abstract DeclaredField<A> propagate(Scope enclosingScope);

	FieldVariant<A> variant(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (!declaration.validateVariantDeclaration(this)) {
			return null;
		}

		final FieldVariant<A> variant = createVariant(declaration, definition);

		if (variant == null) {
			return null;
		}
		this.variants.add(variant);

		return variant;
	}

}
