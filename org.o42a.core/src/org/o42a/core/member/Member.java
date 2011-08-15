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
package org.o42a.core.member;

import static org.o42a.util.use.User.dummyUser;

import java.util.*;

import org.o42a.core.*;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.use.UseInfo;
import org.o42a.util.use.UserInfo;


public abstract class Member extends Placed {

	private static final MemberId[] NO_ALIAS_IDS = new MemberId[0];
	private static final MemberKey[] NO_ALIAS_KEYS = new MemberKey[0];
	private static final Member[] NOTHING_OVERRIDDEN = new Member[0];

	private final MemberOwner owner;

	private MemberAnalysis analysis;
	private Member firstDeclaration;
	private Member lastDefinition;
	private Member[] overridden;

	public Member(
			LocationInfo location,
			Distributor distributor,
			MemberOwner owner) {
		super(location, distributor);
		this.owner = owner;
	}

	public final MemberOwner getMemberOwner() {
		return this.owner;
	}

	public abstract MemberId getId();

	public abstract MemberKey getKey();

	public MemberId[] getAliasIds() {
		return NO_ALIAS_IDS;
	}

	public MemberKey[] getAliasKeys() {
		return NO_ALIAS_KEYS;
	}

	public final String getDisplayName() {
		return getId().toString();
	}

	public final String getDisplayPath() {

		final StringBuilder out = new StringBuilder();
		final Scope enclosingScope = getScope();

		if (enclosingScope == getContext().getRoot().getScope()) {
			out.append("$$");
		} else {

			final Member enclosingMember = enclosingScope.toMember();

			if (enclosingMember == null) {
				out.append(enclosingScope);
			} else {
				out.append(enclosingMember.getDisplayPath());
			}
			out.append(':');
		}
		out.append(getDisplayName());

		return out.toString();
	}

	public abstract Member getWrapped();

	public final MemberAnalysis getAnalysis() {
		if (this.analysis != null) {
			return this.analysis;
		}
		return this.analysis = new MemberAnalysis(this);
	}

	public abstract MemberField toMemberField();

	public abstract MemberClause toMemberClause();

	public abstract MemberLocal toMemberLocal();

	public abstract Field<?> toField(UserInfo user);

	public abstract LocalScope toLocal(UserInfo user);

	public abstract Clause toClause();

	public abstract Container substance(UserInfo user);

	public abstract Visibility getVisibility();

	public abstract boolean isOverride();

	public final boolean isPropagated() {
		return getPropagatedFrom() != null;
	}

	public abstract boolean isAbstract();

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

		final MemberKey memberKey = getKey();
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

	public Set<CompilerContext> getAllContexts() {
		return Collections.singleton(getContext());
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

	public abstract Member wrap(
			MemberOwner owner,
			UserInfo user,
			Member inherited);

	@Override
	public String toString() {
		if (!isOverride()) {
			return getDisplayPath();
		}

		final StringBuilder out = new StringBuilder();

		out.append(getDisplayPath());
		if (isPropagated()) {
			out.append("{propagated from ");

			boolean comma = false;

			for (Member overridden : getOverridden()) {
				if (!comma) {
					comma = true;
				} else {
					out.append(", ");
				}
				out.append(overridden.getDisplayPath());
			}

			out.append('}');
		}

		return out.toString();
	}

	protected final void useBy(UserInfo user) {
		if (user.toUser().isDummy()) {
			return;
		}
		getAnalysis().useBy(user);
	}

	protected final void useSubstanceBy(UserInfo user) {
		if (user.toUser().isDummy()) {
			return;
		}
		getAnalysis().useSubstanceBy(user);
	}

	protected final void useNestedBy(UseInfo user) {
		getAnalysis().useNestedBy(user);
	}

	protected abstract void merge(Member member);

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
					containerAncestor.type(dummyUser())
					.getObject().member(getKey());

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
					containerSample.typeObject(dummyUser()).member(getKey());

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
