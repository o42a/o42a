/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;


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
		return this.member.getMemberKey();
	}

	public final boolean isPropagated() {
		return this.propagated;
	}

	public final boolean isOverride() {
		return this.propagated || this.member.isOverride();
	}

	public final Member createMember(Obj owner) {
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

		final Member member = registerMember(members);

		if (member == null) {
			return;
		}
		members.registerSymbol(member.getMemberId(), member);
	}

	@Override
	public String toString() {
		if (this.propagated) {
			return this.member + "^";
		}
		return this.member.toString();
	}

	private Member registerMember(ContainerMembers members) {

		final MemberKey key = getKey();

		if (!key.isValid()) {
			return null;
		}
		if (!validateStaticOverride()) {
			return null;
		}

		final Member existing = members.members().get(key);

		if (existing == null) {
			// Member is not registered yet.
			final Member member = createMember(members.getOwner());

			members.register(key, member);

			return member;
		}
		if (!isOverride()) {
			reportAmbiguity();
			return null;
		}

		return mergeOverridden(members, key, existing);
	}

	private void reportAmbiguity() {

		final Member member = getMember();

		if (member.toClause() != null) {
			member.getLogger().ambiguousClause(
					member.getLocation(),
					member.getDisplayName());
		} else {
			member.getLogger().ambiguousMember(
					member.getLocation(),
					member.getDisplayName());
		}
	}

	private Member mergeOverridden(
			ContainerMembers members,
			MemberKey key,
			Member existing) {
		if (!existing.getMemberKey().equals(key)) {
			reportAmbiguity();
			return null;
		}
		if (!existing.isPropagated()) {
			// Existing member is explicit.
			if (isPropagated()) {
				// Explicit member takes precedence over propagated one.
				return null;
			}
			reportAmbiguity();
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
		// The member should take care of merging the definitions.
		return null;
	}

	private boolean validateStaticOverride() {
		if (!isOverride() || isPropagated()) {
			return true;
		}

		final MemberField field = getMember().toField();

		if (field == null) {
			return true;
		}

		for (Member overridden : getMember().getOverridden()) {

			final MemberField f = overridden.toField();

			if (f.isStatic()) {
				getMember().getLogger().error(
						"prohibited_static_override",
						getMember().getLocation().setDeclaration(
								f.getLocation()),
						"Static field can not be overridded");
				return false;
			}
		}

		return true;
	}

}
