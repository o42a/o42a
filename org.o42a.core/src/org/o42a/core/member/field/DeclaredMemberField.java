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

import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.Member;


final class DeclaredMemberField extends MemberField {

	private DeclaredField<?> field;
	private ArtifactKind<?> artifactKind;
	private final FieldBuilder builder;
	private FieldDeclarationStatement statement;

	public DeclaredMemberField(FieldBuilder builder) {
		super(builder.getDeclaration());
		this.builder = builder;
	}

	public ArtifactKind<?> getArtifactKind() {
		if (this.artifactKind != null) {
			return this.artifactKind;
		}

		final ArtifactKind<?> kind;
		final Member[] overridden = getOverridden();

		if (overridden.length > 0) {
			kind = overridden[0].toField().getArtifact().getKind();
		} else {

			final FieldDefinition definition = this.builder.getDefinition();

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

		return this.artifactKind = kind;
	}

	@Override
	public DeclaredField<?> toField() {
		if (this.field != null) {
			return this.field;
		}

		this.field = getArtifactKind().declareField(this);

		final FieldVariant<?> variant = this.field.variant(
				this.builder.getDeclaration(),
				this.builder.getDefinition());

		variant.setStatement(this.statement);

		return this.field;
	}

	final void setStatement(FieldDeclarationStatement statement) {
		this.statement = statement;
	}

}
