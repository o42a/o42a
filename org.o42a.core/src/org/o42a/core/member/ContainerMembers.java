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

import static org.o42a.analysis.use.User.dummyUser;

import java.util.Map;

import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;


public abstract class ContainerMembers {

	private final Obj owner;
	private final MemberEntries members = new MemberEntries();
	private final MemberEntries adapters = new MemberEntries();

	public ContainerMembers(Obj owner) {
		this.owner = owner;
	}

	public final Obj getOwner() {
		return this.owner;
	}

	public final void addMember(Member member) {
		if (member.isTypeParameter()) {
			assert member.isOverride() :
				"Type parameters can not be explicitly declared";
			member.getLogger().error(
					"prohibited_type_parameter_override",
					member,
					"The type parameter can not be defined here. "
					+ "Use a type parameters expression "
					+ "or type definition instead");
			return;
		}
		if (!member.isOverride() && !allowDeclaration(member)) {
			return;
		}

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

	protected void add(Member member, boolean override) {
		addEntry(new MemberEntry(member, override));
	}

	private boolean allowDeclaration(Member member) {

		final ConstructionMode constructionMode =
				getOwner().getConstructionMode();
		final MemberField field = member.toField();

		if (field != null && field.field(dummyUser()).isScopeField()) {
			return true;
		}
		if (constructionMode.isStrict()) {
			member.getLogger().error(
					"prohibited_member_declaration",
					member,
					"Can not declare new members here");
			return false;
		}
		if (field == null) {
			return true;
		}
		if (!constructionMode.canDeclareFields()) {
			member.getLogger().error(
					"prohibited_field_declaration",
					member,
					"Can not declare new fields here");
			return false;
		}
		if (field.isStatic()) {
			if (!getOwner().isStatic()) {
				member.getLogger().error(
						"prohibited_static_in_instance",
						member,
						"Static field can not be declared "
						+ "in non-static object");
				return false;
			}
			if (getOwner().isPrototype()) {
				member.getLogger().error(
						"prohibited_static_in_prototype",
						member,
						"Static field can not be declared inside prototype");
				return false;
			}
		}

		return true;
	}

	private void addEntry(MemberEntry entry) {

		final Member member = entry.getMember();
		final MemberId memberId = member.getMemberId();

		if (memberId.getAdapterId() != null) {
			this.adapters.add(entry);
		} else {
			this.members.add(entry);
		}
	}

}
