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
package org.o42a.core.member;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.impl.MemberPropagatedFromID.memberScopePrefix;

import java.util.ArrayList;
import java.util.Iterator;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.impl.MemberPropagatedFromID;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;


public abstract class Member extends Placed {

	private static final Member[] NOTHING_OVERRIDDEN = new Member[0];

	private final MemberOwner owner;

	private Member firstDeclaration;
	private Member lastDefinition;
	private Member[] overridden;
	private ID id;

	public Member(
			LocationInfo location,
			Distributor distributor,
			MemberOwner owner) {
		super(location, distributor);
		this.owner = owner;
	}

	public final ID getId() {
		if (this.id != null) {
			return this.id;
		}

		final ID prefix = memberScopePrefix(this);

		if (!isOverride()) {
			return this.id = prefix.sub(getMemberId());
		}

		return this.id = prefix.sub(
				ID.id(getMemberId()).suffix(new MemberPropagatedFromID(this)));
	}

	public final MemberOwner getMemberOwner() {
		return this.owner;
	}

	public abstract MemberId getMemberId();

	public abstract MemberKey getMemberKey();

	public final String getDisplayName() {
		return getMemberId().toString();
	}

	public abstract MemberField toField();

	public abstract MemberClause toClause();

	public abstract MemberLocal toLocal();

	public abstract Alias toAlias();

	public abstract Container substance(UserInfo user);

	public abstract Visibility getVisibility();

	public abstract boolean isOverride();

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
		if (this.firstDeclaration != null) {
			return this.firstDeclaration;
		}
		if (!isOverride()) {
			return this.firstDeclaration = this;
		}

		final MemberKey memberKey = getMemberKey();
		final Scope origin = memberKey.getOrigin();

		return this.firstDeclaration = origin.getContainer().member(memberKey);
	}

	/**
	 * The last definition of this member.
	 *
	 * @return the last member's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	public Member getLastDefinition() {
		if (this.lastDefinition != null) {
			return this.lastDefinition;
		}
		if (!isPropagated()) {
			return this.lastDefinition = this;
		}

		final Member[] overridden = getOverridden();

		if (overridden.length != 1) {
			return this.lastDefinition = this;
		}

		return this.lastDefinition = overridden[0].getLastDefinition();
	}

	public final boolean isClone() {
		return getLastDefinition() != this;
	}

	public Member[] getOverridden() {
		if (this.overridden != null) {
			return this.overridden;
		}
		return this.overridden = overriddenMembers();
	}

	public final boolean definedAfter(Member other) {
		return getScope().derivedFrom(other.getDefinedIn());
	}

	public abstract Member propagateTo(MemberOwner owner);

	public abstract void resolveAll();

	@Override
	public String toString() {
		if (this.owner == null) {
			return super.toString();
		}
		return getId().toString();
	}

	private Member[] overriddenMembers() {
		if (!isOverride()) {
			return NOTHING_OVERRIDDEN;
		}

		final ObjectType containerType = getContainer().toObject().type();
		final Sample[] containerSamples = containerType.getSamples();
		final ArrayList<Member> overridden;
		final TypeRef containerAncestor = containerType.getAncestor();

		if (containerAncestor != null) {

			final Member ancestorMember =
					containerAncestor.getType().member(getMemberKey());

			if (ancestorMember != null) {
				overridden = new ArrayList<Member>(containerSamples.length + 1);
				overridden.add(ancestorMember);
			} else {
				overridden = new ArrayList<Member>(containerSamples.length);
			}
		} else {
			overridden = new ArrayList<Member>(containerSamples.length);
		}

		for (Sample containerSample : containerSamples) {

			final Member sampleMember =
					containerSample.typeObject(dummyUser()).member(getMemberKey());

			if (sampleMember == null) {
				continue;
			}
			addMember(overridden, sampleMember);
		}

		return overridden.toArray(new Member[overridden.size()]);
	}

	private void addMember(ArrayList<Member> members, Member member) {

		final Scope definedIn = member.getDefinedIn();
		final Iterator<Member> i = members.iterator();

		while (i.hasNext()) {

			final Member m = i.next();

			if (m.getDefinedIn().derivedFrom(definedIn)) {
				return;
			}
			if (definedIn.derivedFrom(m.getDefinedIn())) {
				i.remove();
				while (i.hasNext()) {

					final Member f2 = i.next();

					if (definedIn.derivedFrom(f2.getDefinedIn())) {
						i.remove();
					}
				}

				break;
			}
		}

		members.add(member);
	}

}
