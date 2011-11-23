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

import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.util.use.User.dummyUser;

import java.util.HashSet;
import java.util.Set;

import org.o42a.core.Container;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.ArrayUtil;
import org.o42a.util.use.UserInfo;


public abstract class MemberField extends Member {

	private static final MemberField[] NO_MERGED = new MemberField[0];

	private final FieldDeclaration declaration;
	private Field<?> field;
	private MemberKey key;
	private Visibility visibility;
	private MemberField[] mergedWith = NO_MERGED;
	private HashSet<CompilerContext> allContexts;

	private FieldAnalysis analysis;

	public MemberField(MemberOwner owner, FieldDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	protected MemberField(
			LocationInfo location,
			MemberOwner owner,
			MemberField propagatedFrom) {
		super(
				location,
				propagatedFrom.distributeIn(owner.getContainer()), owner);
		this.key = propagatedFrom.getKey();
		this.visibility = propagatedFrom.getVisibility();
		this.declaration =
				new FieldDeclaration(
						propagatedFrom,
						distribute(),
						propagatedFrom.getDeclaration())
				.override();
	}

	@Override
	public final MemberId getId() {
		return getDeclaration().getMemberId();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public abstract ArtifactKind<?> getArtifactKind();

	@Override
	public final Visibility getVisibility() {
		getKey();
		return this.visibility;
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

	@Override
	public final boolean isAbstract() {
		return getDeclaration().isAbstract();
	}

	public final boolean isPrototype() {
		return getDeclaration().isPrototype();
	}

	@Override
	public final boolean isOverride() {
		return getDeclaration().isOverride();
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
	public final MemberField getFirstDeclaration() {
		return super.getFirstDeclaration().toField();
	}

	@Override
	public final MemberField getLastDefinition() {
		return super.getLastDefinition().toField();
	}

	public final FieldAnalysis getAnalysis() {
		if (this.analysis != null) {
			return this.analysis;
		}

		final MemberField lastDefinition = getLastDefinition();

		if (lastDefinition != this) {
			return this.analysis = lastDefinition.getAnalysis();
		}

		return this.analysis = new FieldAnalysis(this);
	}

	public final Field<?> field(UserInfo user) {
		if (this.field != null) {
			useBy(user);
			return this.field;
		}

		setField(user, createField());

		return this.field;
	}

	@Override
	public final MemberField toField() {
		return this;
	}

	@Override
	public final MemberClause toClause() {
		return null;
	}

	@Override
	public final MemberLocal toLocal() {
		return null;
	}

	@Override
	public final Container substance(UserInfo user) {
		return field(user).getContainer();
	}

	@Override
	public abstract MemberField getPropagatedFrom();

	@Override
	public final Set<CompilerContext> getAllContexts() {
		if (this.allContexts != null) {
			return this.allContexts;
		}

		this.allContexts = new HashSet<CompilerContext>(1);
		this.allContexts.add(getContext());

		return this.allContexts;
	}

	@Override
	public abstract MemberField propagateTo(MemberOwner owner);

	@Override
	public void resolveAll() {

		final Artifact<?> artifact = field(dummyUser()).getArtifact();

		getAnalysis().useSubstanceBy(artifact.content());
		getAnalysis().useNestedBy(artifact.fieldUses());
		artifact.resolveAll();
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
			if (isClone()) {
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

		final MemberField memberField = member.toField();

		if (memberField == null) {
			getLogger().error(
					"not_field",
					member,
					"'%s' is not a field",
					member.getDisplayName());
			return;
		}

		final CompilerContext memberContext = member.getContext();

		if (getContext() != memberContext) {
			if (this.allContexts == null) {
				this.allContexts = new HashSet<CompilerContext>(2);
				this.allContexts.add(getContext());
			}
			this.allContexts.add(memberContext);
		}

		if (this.field != null) {
			mergeField(memberField);
		} else {
			this.mergedWith = ArrayUtil.append(this.mergedWith, memberField);
		}
	}

	protected final void setField(UserInfo user, Field<?> field) {
		this.field = field;
		useBy(user);
		for (MemberField merged : getMergedWith()) {
			mergeField(merged);
		}
	}

	protected abstract Field<?> createField();

	protected final MemberField[] getMergedWith() {
		return this.mergedWith;
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
		this.field.merge(member.field(dummyUser()));
	}

	private void useBy(UserInfo user) {
		if (user.toUser().isDummy()) {
			return;
		}
		getAnalysis().useBy(user.toUser());
	}

}
