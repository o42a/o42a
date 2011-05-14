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

import java.util.Map;

import org.o42a.core.Container;
import org.o42a.util.Chain;


public abstract class ContainerMembers {

	private final MemberOwner owner;
	private final MemberEntries members = new MemberEntries();
	private final MemberEntries adapters = new MemberEntries();

	public ContainerMembers(MemberOwner owner) {
		this.owner = owner;
	}

	public final MemberOwner getOwner() {
		return this.owner;
	}

	public final Container getContainer() {
		return this.owner.getContainer();
	}

	public final void addMember(Member member) {
		addMember(new MemberEntry(member, false));
	}

	public final void propagateMember(Member overridden) {
		addMember(new MemberEntry(overridden, true));
	}

	public void registerMembers(boolean registerAdapters) {
		this.members.registerMembers(this);
		if (registerAdapters) {
			this.adapters.registerMembers(this);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName());
		out.append('{');
		if (!this.members.isEmpty()) {
			out.append("members: ");
			out.append(this.members);
		}
		if (!this.adapters.isEmpty()) {
			if (!this.members.isEmpty()) {
				out.append(' ');
			}
			out.append("adapters: ");
			out.append(this.adapters);
		}
		out.append('}');

		return out.toString();
	}

	protected abstract Map<MemberKey, Member> members();

	protected abstract Map<MemberId, Symbol> symbols();

	protected boolean register(Member member) {
		members().put(member.getKey(), member);
		return true;
	}

	protected void registerSymbol(Member member) {
		if (member.getVisibility() == Visibility.PRIVATE
				&& member.isOverride()) {
			// Only explicitly declared private members registered.
			return;
		}

		final MemberId memberId = member.getKey().getMemberId();
		Symbol symbol = symbols().get(memberId);

		if (symbol != null) {
			symbol.addMember(member);
		} else {
			symbol = new Symbol(member);
			symbols().put(memberId, symbol);
		}
	}

	private void addMember(MemberEntry entry) {
		if (entry.getMember().getId().getAdapterId() != null) {
			this.adapters.add(entry);
		} else {
			this.members.add(entry);
		}
	}

	private static final class MemberEntries extends Chain<MemberEntry> {

		private MemberEntry registering;

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append('{');
			if (this.registering != null) {
				out.append("registering: [");

				MemberEntry entry = this.registering;
				boolean comma = false;

				do {
					if (comma) {
						out.append(", ");
					} else {
						comma = true;
					}
					out.append(entry);
					entry = entry.next;
				} while (entry != null);

				out.append(']');
			}
			if (!isEmpty()) {
				if (this.registering != null) {
					out.append(' ');
				}
				out.append("pending: ");
				out.append(super.toString());
			}
			out.append('}');

			return out.toString();
		}

		@Override
		protected MemberEntry next(MemberEntry item) {
			return item.next;
		}

		@Override
		protected void setNext(MemberEntry prev, MemberEntry next) {
			prev.next = next;
		}

		void registerMembers(ContainerMembers members) {

			for (;;) {
				if (this.registering == null) {
					// Registration is not in progress.
					if (isEmpty()) {
						// No pending members to register. Exit.
						return;
					}
					// Register first pending.
					this.registering = getFirst();
					empty();
				} else {
					// Register next pending.
					this.registering = this.registering.removeNext();
					if (this.registering == null) {
						// Nothing to register.
						// Try for more pending members.
						continue;
					}
				}

				this.registering.register(members);
			}
		}

	}

}
