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

import static org.o42a.core.member.AdapterId.adapterTypeScope;

import java.util.EnumMap;
import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;


public final class Symbol {

	private final MemberId memberId;
	private final EnumMap<Accessor, Member> members =
			new EnumMap<>(Accessor.class);
	private final EnumMap<Accessor, IdentityHashMap<Scope, Member>> all =
			new EnumMap<>(Accessor.class);

	Symbol(MemberId memberId, Member member) {
		this.memberId = memberId;
		registerMember(member);
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final Member member(Accessor accessor) {
		return this.members.get(accessor);
	}

	public final Member member(Obj declaredIn, Accessor accessor) {
		if (declaredIn == null) {
			return member(accessor);
		}

		final IdentityHashMap<Scope, Member> members = this.all.get(accessor);

		if (members == null) {
			return null;
		}

		return members.get(adapterTypeScope(declaredIn));
	}

	@Override
	public String toString() {

		Accessor accessor = null;

		for (Accessor e : this.members.keySet()) {
			if (accessor == null) {
				accessor = e;
			} else if (e.implies(accessor)) {
				accessor = e;
			}
		}

		final StringBuilder out = new StringBuilder();

		out.append("Symbol[").append(getMemberId()).append(": ");
		out.append(accessor).append(']');

		return out.toString();
	}

	final void addMember(MemberId memberId, Member member) {
		assert getMemberId().equals(memberId) :
			"Wrong field: \"" + member.getDisplayName()
			+ "\", but \"" + getMemberId() + "\" expected";
		registerMember(member);
	}

	private void registerMember(Member member) {
		if (!registerMember(member, Accessor.OWNER)) {
			return;
		}

		switch (member.getVisibility()) {
		case PRIVATE:
			registerMember(member, Accessor.DECLARATION);
			registerMember(member, Accessor.ENCLOSED);
			break;
		case PROTECTED:
			registerMember(member, Accessor.INHERITANT);
			registerMember(member, Accessor.ENCLOSED);
			if (!member.isPropagated()) {
				registerMember(member, Accessor.DECLARATION);
			}
			break;
		case PUBLIC:
			registerMember(member, Accessor.PUBLIC);
			registerMember(member, Accessor.DECLARATION);
			registerMember(member, Accessor.INHERITANT);
			registerMember(member, Accessor.ENCLOSED);
		}
	}

	private boolean registerMember(Member member, Accessor accessor) {
		if (!register(member, accessor)) {
			return false;
		}

		final Member old = this.members.put(accessor, member);

		if (old == null) {
			return true;
		}
		if (!member.isOverride()) {
			return true;
		}
		if (old.isOverride()) {
			if (member.getMemberKey().getOrigin().derivedFrom(
					old.getMemberKey().getOrigin())) {
				return true;
			}
		}

		this.members.put(accessor, old);

		return false;
	}

	private boolean register(Member member, Accessor accessor) {

		final IdentityHashMap<Scope, Member> members = this.all.get(accessor);
		final IdentityHashMap<Scope, Member> newMembers;

		if (members != null) {
			newMembers = members;
		} else {
			newMembers = new IdentityHashMap<>();
			this.all.put(accessor, newMembers);
		}

		final Member old = newMembers.put(member.getMemberKey().getOrigin(), member);

		assert old == null || old.isPropagated() :
			old + " is not propagated, but replaced with " + member;

		return true;
	}

}
