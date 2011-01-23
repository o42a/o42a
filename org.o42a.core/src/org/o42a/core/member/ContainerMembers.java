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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.util.ArrayUtil;


public abstract class ContainerMembers {

	private final Container container;
	private final HashMap<MemberKey, Entry[]> registry =
		new HashMap<MemberKey, Entry[]>();

	public ContainerMembers(Container container) {
		this.container = container;
	}

	public final Container getContainer() {
		return this.container;
	}

	public final void addMember(Member member) {
		addMember(new Entry(member, false));
	}

	public final void propagateMember(Member overridden) {
		addMember(new Entry(overridden, true));
	}

	public void addMembers(Iterable<? extends Member> members) {
		for (Member member : members) {
			addMember(member);
		}
	}

	public void registerMembers() {

		final Scope scope = getContainer().getScope();

		for (Entry[] members : this.registry.values()) {

			Member member = null;

			for (Entry e : members) {

				final Member m = e.createMember(scope);

				if (member == null) {
					member = m;
					continue;
				}
				if (m == null) {
					continue;
				}
				member.merge(m);
			}
			if (member != null) {
				register(member);
			}
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append(getClass().getSimpleName());
		out.append('[');
		for (Map.Entry<MemberKey, Entry[]> e : this.registry.entrySet()) {
			if (comma) {
				out.append(", ");
			} else {
				comma = true;
			}
			out.append(e.getKey()).append('=');
			out.append(Arrays.toString(e.getValue()));
		}
		out.append(']');

		return out.toString();
	}

	protected abstract Map<MemberKey, Member> members();

	protected abstract Map<MemberId, Symbol> symbols();

	protected boolean register(Member member) {
		if (!registerMember(member)) {
			return false;
		}
		registerSymbol(member);
		return true;
	}

	private void addMember(Entry entry) {

		final MemberKey key = entry.getKey();

		if (!key.isValid()) {
			return;
		}

		final Entry[] registered = this.registry.get(key);

		if (registered == null) {
			this.registry.put(key, new Entry[] {entry});
		} else if (entry.isOverride()) {
			updateOverridden(registered, entry);
		} else {
			this.registry.put(key, ArrayUtil.append(registered, entry));
		}
	}

	private void updateOverridden(Entry[] registered, Entry entry) {

		for (int i = registered.length - 1; i >= 0 ; --i) {

			final Entry e = registered[i];

			if (e.definedAfter(entry)) {
				// The already registered member is defined after the new one.
				// Leave it in registry.
				return;
			}
			if (entry.definedAfter(e)) {
				// The new member is defined after the already registered one.
				// Update the registry with the new member.
				registered = ArrayUtil.remove(registered, i);
			}
		}

		this.registry.put(entry.getKey(), ArrayUtil.append(registered, entry));
	}

	private boolean registerMember(Member member) {

		final MemberKey key = member.getKey();
		final Member old = members().put(key, member);

		if (old != null) {
			members().put(key, old);
			this.container.getContext().getLogger().ambiguousMember(
					member,
					member.getDisplayName());
			return false;
		}

		return true;
	}

	private void registerSymbol(Member member) {
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

	private static final class Entry {

		private final Member member;
		private final boolean propagated;

		Entry(Member member, boolean propagated) {
			this.member = member;
			this.propagated = propagated;
		}

		public final MemberKey getKey() {
			return this.member.getKey();
		}

		public final Member getMember() {
			return this.member;
		}

		public final boolean isOverride() {
			return this.propagated || this.member.isOverride();
		}

		public final Member createMember(Scope scope) {
			if (!this.propagated) {
				return this.member;
			}
			return this.member.propagateTo(scope);
		}

		public final boolean definedAfter(Entry entry) {
			return this.member.getScope().derivedFrom(
					entry.getMember().getDefinedIn());
		}

		@Override
		public String toString() {
			if (this.propagated) {
				return this.member + "^";
			}
			return this.member.toString();
		}

	}

}
