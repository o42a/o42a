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

import static org.o42a.core.artifact.ArtifactKind.*;
import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.ArrayUtil;
import org.o42a.util.use.UserInfo;


public abstract class MemberField extends Member {

	private static final MemberField[] NO_MERGED = new MemberField[0];

	private final FieldDeclaration declaration;
	private Field<?> field;
	private MemberKey key;
	private Visibility visibility;
	private MemberField[] mergedWith = NO_MERGED;

	public MemberField(MemberOwner owner, FieldDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	MemberField(MemberOwner owner, Field<?> field, MemberField overridden) {
		super(overridden, overridden.distributeIn(owner.getContainer()), owner);
		this.field = field;
		this.key = overridden.getKey();
		this.visibility = overridden.getVisibility();
		this.declaration =
			new FieldDeclaration(
					overridden,
					distribute(),
					overridden.getDeclaration())
			.override();
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

	@Override
	public final MemberId getId() {
		return getDeclaration().getMemberId();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final Visibility getVisibility() {
		getKey();
		return this.visibility;
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		if (getDeclaration().isOverride()) {
			return this.key = overrideField();
		}
		return this.key = declareNewField();
	}

	@Override
	public abstract MemberField getWrapped();

	@Override
	public final MemberField toMemberField() {
		return this;
	}

	@Override
	public final MemberClause toMemberClause() {
		return null;
	}

	@Override
	public final MemberLocal toMemberLocal() {
		return null;
	}

	@Override
	public final Field<?> toField(UserInfo user) {
		if (this.field != null) {
			use(user);
			return this.field;
		}

		setField(user, createField());

		return this.field;
	}

	@Override
	public final LocalScope toLocal(UserInfo user) {
		return null;
	}

	@Override
	public final Clause toClause() {
		return null;
	}

	@Override
	public final Container substance(UserInfo user) {
		return toField(user).getContainer();
	}

	@Override
	public boolean isOverride() {
		return this.declaration.isOverride();
	}

	@Override
	public final boolean isAbstract() {
		return getDeclaration().isAbstract();
	}

	@Override
	public Member getPropagatedFrom() {
		return null;
	}

	@Override
	public MemberField propagateTo(MemberOwner owner) {

		final Field<?> field = toField(owner.getScope().dummyResolver());

		return field.propagateTo(owner).toMember();
	}

	@Override
	public void resolveAll() {
		toField(dummyUser()).getArtifact().resolveAll();
	}

	@Override
	public MemberField wrap(
			MemberOwner owner,
			UserInfo user,
			Member inherited) {

		final ArtifactKind<?> artifactKind = toField(user).getArtifactKind();

		if (artifactKind == OBJECT) {
			return new ObjectFieldWrap(
					owner,
					inherited.toField(user).toKind(OBJECT),
					toField(user).toKind(OBJECT)).toMember();
		}
		if (artifactKind == LINK) {
			return new LinkFieldWrap(
					getMemberOwner(),
					inherited.toField(user).toKind(LINK),
					toField(user).toKind(LINK)).toMember();
		}
		if (artifactKind == VARIABLE) {
			return new LinkFieldWrap(
					getMemberOwner(),
					inherited.toField(user).toKind(VARIABLE),
					toField(user).toKind(VARIABLE)).toMember();
		}
		if (artifactKind == ARRAY) {
			return new ArrayFieldWrap(
					getMemberOwner(),
					inherited.toField(user).toKind(ARRAY),
					toField(user).toKind(ARRAY)).toMember();
		}

		throw new IllegalStateException("Can not wrap " + this);
	}

	@Override
	public String toString() {
		if (!isOverride()) {
			return getDisplayPath();
		}

		final StringBuilder out = new StringBuilder();

		out.append(getDisplayPath());
		out.append('{');

		if (this.key != null) {
			out.append(this.key.getOrigin());
		} else {
			out.append(getDeclaration().getDeclaredIn());
		}

		if (isPropagated()) {
			out.append(", ");
			if (toField(dummyUser()).isClone()) {
				out.append("clone of ");
				out.append(getLastDefinition().getDisplayPath());
			} else {
				out.append("propagated from ");

				boolean comma = false;

				for (Member overridden : getOverridden()) {
					if (!comma) {
						comma = true;
					} else {
						out.append(", ");
					}
					out.append(overridden.getDisplayPath());
				}
			}
		}

		out.append('}');

		return out.toString();
	}

	@Override
	protected void merge(Member member) {

		final MemberField memberField = member.toMemberField();

		if (memberField == null) {
			getLogger().error(
					"not_field",
					member,
					"'%s' is not a field",
					member.getDisplayName());
			return;
		}
		if (this.field != null) {
			mergeField(memberField);
		} else {
			this.mergedWith = ArrayUtil.append(this.mergedWith, memberField);
		}
	}

	protected ArtifactKind<?> determineArtifactKind() {
		return toField(dummyUser()).getArtifactKind();
	}

	protected final void setField(UserInfo user, Field<?> field) {
		this.field = field;
		use(user);
		for (MemberField merged : getMergedWith()) {
			mergeField(merged);
		}
	}

	protected abstract Field<?> createField();

	final MemberField[] getMergedWith() {
		return this.mergedWith;
	}

	final void setArtifact(Artifact<?> artifact) {
		useSubstanceBy(artifact.content());
		useNestedBy(artifact.fieldUses());
	}

	private MemberKey overrideField() {

		final Member overridden = overridden();

		if (overridden != null) {
			this.visibility = overridden.getVisibility();
			return overridden.getKey();
		}

		this.visibility = Visibility.PRIVATE;

		return brokenMemberKey();
	}

	private Member overridden() {

		Member overridden = null;
		final StaticTypeRef declaredInRef = getDeclaration().getDeclaredIn();

		if (declaredInRef != null) {

			final Obj declaredIn = declaredInRef.typeObject(dummyUser());

			if (declaredIn == null) {
				return null;
			}
			overridden = declaredIn.member(getId(), Accessor.INHERITANT);
		} else {

			final ObjectType containerType = getContainer().toObject().type();

			for (Sample sample : containerType.getSamples()) {
				overridden = overridden(
						overridden,
						sample.typeObject(dummyUser()));
			}

			final TypeRef ancestor = containerType.getAncestor();

			if (ancestor != null) {
				overridden = overridden(
						overridden,
						ancestor.typeObject(dummyUser()));
			}
		}

		if (overridden == null) {
			getLogger().cantOverrideUnknown(this, getDisplayName());
		}

		return overridden;
	}

	private Member overridden(Member overridden, Obj ascendant) {
		if (ascendant == null) {
			return overridden;
		}

		final Member member = ascendant.member(getId(), Accessor.INHERITANT);

		if (member == null) {
			return overridden;
		}
		if (overridden == null) {
			return member;
		}
		if (overridden.definedAfter(member)) {
			return overridden;
		}
		if (member.definedAfter(overridden)) {
			return member;
		}

		return overridden;
	}

	private MemberKey declareNewField() {
		this.visibility = getDeclaration().getVisibility();
		return getId().key(getScope());
	}

	private void mergeField(MemberField member) {
		this.field.merge(member.toField(dummyUser()));
	}

	private void use(UserInfo user) {
		useBy(user);
	}

}
