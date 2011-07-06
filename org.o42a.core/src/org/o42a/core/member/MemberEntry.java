/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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


final class MemberEntry {

	private final Member member;
	private final boolean propagated;
	MemberEntry next;
	private boolean registered;

	MemberEntry(Member member, boolean propagated) {
		this.member = member;
		this.propagated = propagated;
	}

	public final Member getMember() {
		return this.member;
	}

	public final MemberEntry removeNext() {

		final MemberEntry next = this.next;

		if (next != null) {
			this.next = next.next;
		}

		return next;
	}

	public final MemberKey getKey() {
		return this.member.getKey();
	}

	public final boolean isPropagated() {
		return this.propagated;
	}

	public final boolean isOverride() {
		return this.propagated || this.member.isOverride();
	}

	public final Member createMember(MemberOwner owner) {
		if (!this.propagated) {
			return this.member;
		}

		final Member propagated = this.member.propagateTo(owner);

		assert propagated.getPropagatedFrom() == this.member :
			propagated + " is not propagated from " + this.member;

		return propagated;
	}

	public void register(ContainerMembers members) {
		assert !this.registered :
			"Member already registered: " + this;
		this.registered = true;

		final MemberKey key = getKey();

		if (!key.isValid()) {
			return;
		}

		final Member member;

		if (!isOverride()) {
			member = registerNew(members, key);
		} else {
			member = registerOverridden(members, key);
		}
		if (member == null) {
			return;
		}
		for (MemberKey aliasKey : member.getAliasKeys()) {
			members.register(aliasKey, member);
		}

		members.registerSymbol(member.getId(), member);
		for (MemberId aliasId : member.getAliasIds()) {
			members.registerSymbol(aliasId, member);
		}
	}

	@Override
	public String toString() {
		if (this.propagated) {
			return this.member + "^";
		}
		return this.member.toString();
	}

	private Member registerNew(ContainerMembers members, MemberKey key) {

		final Member existing = members.members().get(key);
		final Member member = createMember(members.getOwner());

		if (existing != null) {
			// Merge with existing member.
			existing.merge(member);
			return null;
		}

		// Register new member.
		members.register(key, member);

		return member;
	}

	private Member registerOverridden(
			ContainerMembers members,
			MemberKey key) {

		final Member existing = members.members().get(key);

		if (existing == null) {
			// Member is not registered yet.

			final Member member = createMember(members.getOwner());

			members.register(key, member);

			return member;
		}

		// Member already registered.
		if (!existing.isPropagated()) {
			// Existing member is explicit.
			if (isPropagated()) {
				// Explicit member takes precedence over propagated one.
				return null;
			}

			// Merge explicit member definitions.
			final Member member = createMember(members.getOwner());

			existing.merge(member);

			return null;
		}

		// Existing member is propagated.
		if (!isPropagated()) {
			// Explicit member takes precedence over propagated one.

			final Member member = createMember(members.getOwner());

			members.register(key, member);

			return member;
		}

		// Both members are propagated.
		// Determine the one defined last.
		final Member propagatedFrom = existing.getPropagatedFrom();

		if (propagatedFrom.definedAfter(getMember())) {
			// Already registered member is defined after the new one.
			// Leave it in registry.
			return null;
		}
		if (getMember().definedAfter(propagatedFrom)) {
			// New member is defined after the already registered one.
			// Update the registry with the new member.
			final Member member = createMember(members.getOwner());

			members.register(key, member);

			return member;
		}

		// Otherwise, leave it as is.
		// The member should take care of issuing an error.
		return null;
	}

}
