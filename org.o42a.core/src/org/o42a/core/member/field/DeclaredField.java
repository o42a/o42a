/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.core.st.sentence.Statements;


public class DeclaredField<A extends Artifact<A>> extends Field<A> {

	public static DeclaredField<?> declareField(FieldDeclaration declaration) {
		return new DeclaredMemberField(declaration).toField();
	}

	private final ArrayList<FieldVariant<A>> variants =
		new ArrayList<FieldVariant<A>>();
	private ArtifactKind<A> artifactKind;
	private FieldDecl<A> decl;

	DeclaredField(DeclaredMemberField member) {
		super(member);
	}

	private DeclaredField(
			Container enclosingContainer,
			DeclaredField<A> sample) {
		super(enclosingContainer, sample);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ArtifactKind<A> getArtifactKind() {
		if (this.artifactKind != null) {
			return this.artifactKind;
		}

		final ArtifactKind<?> kind;
		final Field<A>[] overridden = getOverridden();

		if (overridden.length > 0) {
			kind = overridden[0].getArtifact().getKind();
		} else {

			final FieldDefinition definition =
				getVariants().get(0).getDefinition();

			if (definition.isArray()) {
				kind = ArtifactKind.ARRAY;
			} else {

				final Artifact<?> value =
					definition.getValue().getResolution().toArtifact();

				if (value.getKind() == ArtifactKind.ARRAY) {
					kind = ArtifactKind.ARRAY;
				} else if (getDeclaration().isLink()) {
					kind = ArtifactKind.LINK;
				} else if (getDeclaration().isVariable()) {
					kind = ArtifactKind.VARIABLE;
				} else {
					kind = ArtifactKind.OBJECT;
				}
			}
		}
		if (!kind.is(ArtifactKind.OBJECT)) {
			if (getDeclaration().isLink() && !kind.is(ArtifactKind.LINK)) {
				getLogger().prohibitedLinkType(getDeclaration());
			}
			if (getDeclaration().isVariable()
					&& !kind.is(ArtifactKind.VARIABLE)) {
				getLogger().prohibitedVariableType(getDeclaration());
			}
		}

		return this.artifactKind = (ArtifactKind<A>) kind;
	}

	@SuppressWarnings("unchecked")
	@Override
	public A getArtifact() {
		if (getScopeArtifact() == null) {
			if (!getKey().isValid()) {

				final Artifact<?> falseObject = getContext().getFalse();

				setScopeArtifact((A) falseObject);
			} else if (isOverride()) {
				setScopeArtifact(getDecl().overrideArtifact());
			} else {
				setScopeArtifact(getDecl().declareArtifact());
			}
			for (FieldVariant<A> variant : getVariants()) {
				variant.init();
			}
		}

		return getScopeArtifact();
	}

	public List<FieldVariant<A>> getVariants() {
		return this.variants;
	}

	public FieldVariant<A> variant(
			Statements<?> enclosing,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (!declaration.validateVariantDeclaration(this)) {
			return null;
		}

		final FieldVariant<A> variant = new FieldVariant<A>(
				this,
				enclosing,
				declaration,
				definition);

		addVariant(variant);

		return variant;
	}

	@Override
	protected Field<A> propagate(Scope enclosingScope) {
		return new DeclaredField<A>(enclosingScope.getContainer(), this);
	}

	@Override
	protected A propagateArtifact(Field<A> overridden) {
		return getDecl().propagateArtifact();
	}

	@Override
	protected void merge(Field<?> field) {
		if (!(field instanceof DeclaredField<?>)) {
			getLogger().ambiguousField(field, getDisplayName());
			return;
		}

		final DeclaredField<?> declaredField = (DeclaredField<?>) field;

		getDecl().merge(declaredField.getDecl());
	}

	final FieldDecl<A> getDecl() {
		if (this.decl == null) {
			this.decl = getArtifactKind().fieldDecl(this);
		}
		return this.decl;
	}

	void addVariant(FieldVariant<A> variant) {
		this.variants.add(variant);
	}


}
