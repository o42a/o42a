/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.member;

import static org.o42a.core.member.MemberPath.SELF_MEMBER_PATH;
import static org.o42a.core.member.impl.MemberPropagatedFromID.memberScopePrefix;
import static org.o42a.util.fn.Init.init;
import static org.o42a.util.fn.NullableInit.nullableInit;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.*;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.impl.MemberPropagatedFromID;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.fn.Init;
import org.o42a.util.fn.NullableInit;
import org.o42a.util.string.ID;


public abstract class Member extends Contained {

	private final Obj owner;

	private final Init<ID> id = init(this::createId);
	private final Init<Member> firstDeclaration =
			init(this::findFirstDeclaration);
	private final Init<Member> lastDefinition = init(this::findLastDefinition);
	private final NullableInit<Member> overridden =
			nullableInit(this::findOverridden);

	public Member(
			LocationInfo location,
			Distributor distributor,
			Obj owner) {
		super(location, distributor);
		this.owner = owner;
	}

	public final ID getId() {
		return this.id.get();
	}

	public final Obj getMemberOwner() {
		return this.owner;
	}

	public abstract MemberId getMemberId();

	public abstract MemberKey getMemberKey();

	public abstract MemberPath getMemberPath();

	/**
	 * Determines whether this member matches the given identifier and optional
	 * placement and returns the member path leading to this member if so.
	 *
	 * @param memberId target member identifier or <code>null</code> to match
	 * unconditionally.
	 * @param declaredIn the object this member should be declared in
	 * or <code>null</code> to match against identifier only.
	 *
	 * @return a path to this member or <code>null</code> if it does not match
	 * the request.
	 */
	public final MemberPath matchingPath(MemberId memberId, Obj declaredIn) {
		if (declaredIn != null
				&& !getMemberOwner().type().derivedFrom(declaredIn.type())) {
			// The given object does not contain this member.
			return null;
		}
		if (memberId == null) {
			return SELF_MEMBER_PATH;
		}
		if (memberId.getEnclosingId() == null) {

			final MemberId enclosingId = getMemberId().getEnclosingId();

			if (!getMemberId().getLocalId().equals(memberId)) {
				return null;
			}
			if (enclosingId == null) {
				return SELF_MEMBER_PATH;
			}
			return getMemberPath();
		}
		if (!getMemberId().equals(memberId)) {
			return null;
		}
		return getMemberPath();
	}

	public final String getDisplayName() {
		return getMemberId().toString();
	}

	/**
	 * Whether this is a member of type.
	 *
	 * <p>Type members can be accessed even if the owner object is prototype.
	 * </p>
	 *
	 * @return return <code>true</code> if this is a type member,
	 * or <code>false</code> otherwise.
	 */
	public final boolean isTypeMember() {
		return isTypeParameter();
	}

	/**
	 * Whether this is an alias.
	 *
	 * @return <code>true</code> if this is an alias, or <code>false</code>
	 * otherwise.
	 */
	public abstract boolean isAlias();

	/**
	 * Whether this member is type parameter.
	 *
	 * <p>Type parameters are {@link #isTypeMember() type members}.</p>
	 *
	 * <p>Type parameters can only be declared or overridden inside a type
	 * definition block like this:
	 * <pre><code>
	 * Prefix #(
	 *   Type parameter := void
	 * )
	 * </code></pre></p>
	 *
	 * @return <code>true</code> if this member is a type parameter,
	 * or <code>false</code> otherwise.
	 */
	public final boolean isTypeParameter() {
		return toTypeParameter() != null;
	}

	public abstract MemberTypeParameter toTypeParameter();

	public abstract MemberField toField();

	public abstract MemberAlias toAlias();

	public abstract MemberLocal toLocal();

	public abstract MemberClause toClause();

	public abstract Container substance(UserInfo user);

	public abstract Visibility getVisibility();

	public abstract boolean isOverride();

	public boolean isStatic() {
		return false;
	}

	public final boolean isPropagated() {
		return getPropagatedFrom() != null;
	}

	/**
	 * The scope this members's definition were assigned in.
	 *
	 * @return the {@link #getLastDefinition() last definition} scope.
	 */
	public final Scope getDefinedIn() {
		return getLastDefinition().getScope();
	}

	public abstract Member getPropagatedFrom();

	public Member getFirstDeclaration() {
		return this.firstDeclaration.get();
	}

	/**
	 * The last definition of this member.
	 *
	 * @return the last member's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	public Member getLastDefinition() {
		return this.lastDefinition.get();
	}

	public final boolean isClone() {
		return getLastDefinition() != this;
	}

	public final Member getOverridden() {
		return this.overridden.get();
	}

	public final boolean definedAfter(Member other) {
		return getScope().derivedFrom(other.getDefinedIn());
	}

	public abstract Member propagateTo(Obj owner);

	public abstract void resolveAll();

	@Override
	public String toString() {
		if (this.owner == null) {
			return super.toString();
		}
		return getId().toString();
	}

	private ID createId() {

		final ID prefix = memberScopePrefix(this);

		if (!isOverride()) {
			return prefix.sub(getMemberId());
		}

		return prefix.sub(
				ID.id(getMemberId()).suffix(new MemberPropagatedFromID(this)));
	}

	private Member findFirstDeclaration() {
		if (!isOverride()) {
			return this;
		}

		final MemberKey memberKey = getMemberKey();

		return memberKey.getOrigin().getContainer().member(memberKey);
	}

	private Member findLastDefinition() {
		if (!isPropagated()) {
			return this;
		}

		final Member overridden = getOverridden();

		if (overridden == null) {
			return this;
		}

		return overridden.getLastDefinition();
	}

	private Member findOverridden() {
		if (!isOverride()) {
			return null;
		}

		final ObjectType containerType = getContainer().toObject().type();
		final Member ancestorMember = overriddenAncestorMember(containerType);
		final Member sampleMember = overriddenSampleMember(containerType);

		return selectOverridden(ancestorMember, sampleMember);
	}

	private Member overriddenAncestorMember(ObjectType containerType) {

		final TypeRef containerAncestor = containerType.getAncestor();

		if (containerAncestor == null) {
			return null;
		}

		return containerAncestor.getType().member(getMemberKey());
	}

	private Member overriddenSampleMember(ObjectType containerType) {

		final Sample containerSample = containerType.getSample();

		if (containerSample == null) {
			return null;
		}

		return containerSample.getObject().member(getMemberKey());
	}

	private Member selectOverridden(
			Member ancestorMember,
			Member sampleMember) {
		if (sampleMember == null) {
			if (ancestorMember == null) {
				return null;
			}
			return ancestorMember;
		}
		if (ancestorMember == null) {
			return sampleMember;
		}
		if (sampleMember.getDefinedIn().derivedFrom(
				ancestorMember.getDefinedIn())) {
			return sampleMember;
		}
		return ancestorMember;
	}

}
