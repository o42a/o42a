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

	private final Container container;
	private final MemberEntries pending = new MemberEntries();
	private MemberEntry registering;

	public ContainerMembers(Container container) {
		this.container = container;
	}

	public final Container getContainer() {
		return this.container;
	}

	public final void addMember(Member member) {
		this.pending.add(new MemberEntry(member, false));
	}

	public final void propagateMember(Member overridden) {
		this.pending.add(new MemberEntry(overridden, true));
	}

	public void registerMembers() {

		for (;;) {
			if (this.registering == null) {
				// Registration is not in progress.
				if (this.pending.isEmpty()) {
					// No pending members to register. Exit.
					return;
				}
				// Register first pending.
				this.registering = this.pending.getFirst();
				this.pending.empty();
			} else {
				// Register next pending.
				this.registering = this.registering.removeNext();
				if (this.registering == null) {
					// Nothing to register.
					// Try for more pending members.
					continue;
				}
			}

			this.registering.register(this);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName());
		out.append('[');
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

			out.append("]");
		}
		if (!this.pending.isEmpty()) {
			if (this.registering != null) {
				out.append(' ');
			}
			out.append("pending: ");
			out.append(this.pending);
			out.append('}');
		}
		out.append(']');

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

	private static final class MemberEntries extends Chain<MemberEntry> {

		@Override
		protected MemberEntry next(MemberEntry item) {
			return item.next;
		}

		@Override
		protected void setNext(MemberEntry prev, MemberEntry next) {
			prev.next = next;
		}

	}

}
