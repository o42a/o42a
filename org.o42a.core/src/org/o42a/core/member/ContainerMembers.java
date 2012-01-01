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

import java.util.Map;

import org.o42a.core.Container;


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
		addEntry(new MemberEntry(member, false));
	}

	public final void propagateMember(Member overridden) {
		addEntry(new MemberEntry(overridden, true));
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

	protected void register(MemberKey memberKey, Member member) {
		members().put(memberKey, member);
	}

	protected void registerSymbol(MemberId memberId, Member member) {
		if (!member.getVisibility().isOverridable() && member.isOverride()) {
			// Only explicitly declared private members registered.
			return;
		}

		Symbol symbol = symbols().get(memberId);

		if (symbol != null) {
			symbol.addMember(memberId, member);
		} else {
			symbol = new Symbol(memberId, member);
			symbols().put(memberId, symbol);
		}
	}

	private void addEntry(MemberEntry entry) {

		final Member member = entry.getMember();

		addEntry(member.getId(), entry);
		for (MemberId aliasId : member.getAliasIds()) {
			addEntry(aliasId, entry);
		}
	}

	private void addEntry(MemberId id, MemberEntry entry) {
		if (id.getAdapterId() != null) {
			this.adapters.add(entry);
		} else {
			this.members.add(entry);
		}
	}

}
