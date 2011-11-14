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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.Member;


final class DeclaredMemberField extends MemberField {

	private final FieldBuilder builder;
	private ArtifactKind<?> artifactKind;
	private FieldDeclarationStatement statement;

	DeclaredMemberField(FieldBuilder builder) {
		super(builder.getMemberOwner(), builder.getDeclaration());
		this.builder = builder;
	}

	public ArtifactKind<?> getArtifactKind() {
		if (this.artifactKind != null) {
			return this.artifactKind;
		}

		final ArtifactKind<?> kind = determineArtifactKind();

		if (kind == null) {
			return this.artifactKind = ArtifactKind.OBJECT;
		}

		return this.artifactKind = kind;
	}

	@Override
	protected ArtifactKind<?> determineArtifactKind() {

		ArtifactKind<?> kind;
		final Member[] overridden = getOverridden();

		if (overridden.length > 0) {
			kind = overridden[0].toField(dummyUser()).getArtifact().getKind();
		} else if (getDeclaration().isVariable()) {
			kind = ArtifactKind.VARIABLE;
		} else if (getDeclaration().isLink()) {
			kind = ArtifactKind.LINK;
		} else {
			kind = ArtifactKind.OBJECT;
		}

		validateArtifactKind(kind);

		for (MemberField merged : getMergedWith()) {
			kind = determineArtifactKind(merged, kind);
		}

		return kind;
	}

	@Override
	protected Field<?> createField() {

		final DeclaredField<?, ?> field =
				getArtifactKind().declareField(this);
		final FieldVariant<?> variant = field.variant(
				this.builder.getDeclaration(),
				this.builder.getDefinition());

		variant.setStatement(this.statement);

		return field;
	}

	final DeclaredField<?, ?> toDeclaredField() {
		return (DeclaredField<?, ?>) toField(dummyUser());
	}

	final void setStatement(FieldDeclarationStatement statement) {
		this.statement = statement;
	}

	private ArtifactKind<?> determineArtifactKind(
			MemberField member,
			ArtifactKind<?> expectedKind) {

		final ArtifactKind<?> memberKind = member.determineArtifactKind();

		if (memberKind == null) {
			return expectedKind;
		}
		if (expectedKind == null) {
			if (validateArtifactKind(memberKind)) {
				return memberKind;
			}
			return expectedKind;
		}
		if (expectedKind == memberKind) {
			return expectedKind;
		}

		getLogger().error(
				"ambiguous_artifact_kind",
				member,
				"Field artifact kind is ambiguous: " + memberKind
				+ ", while " + expectedKind + " expected");

		return expectedKind;
	}

	private boolean validateArtifactKind(ArtifactKind<?> kind) {
		if (getDeclaration().isLink() && !kind.is(ArtifactKind.LINK)) {
			getLogger().prohibitedLinkType(getDeclaration());
			return false;
		}
		if (getDeclaration().isVariable()
				&& !kind.is(ArtifactKind.VARIABLE)) {
			getLogger().prohibitedVariableType(getDeclaration());
			return false;
		}
		return true;
	}

}
