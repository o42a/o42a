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
package org.o42a.core.artifact.object;

import java.util.ArrayList;
import java.util.HashSet;

import org.o42a.core.LocationSpec;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberRegistry;


public class ObjectMemberRegistry extends MemberRegistry {

	private final HashSet<String> blockNames = new HashSet<String>();
	private int localScopeIndex;
	Obj owner;

	private final ArrayList<Member> members = new ArrayList<Member>();

	public ObjectMemberRegistry(Obj owner) {
		if (owner == null) {
			throw new NullPointerException("Owner not specified");
		}
		this.owner = owner;
	}

	ObjectMemberRegistry() {
		this.owner = null;
	}

	@Override
	public Obj getOwner() {
		return this.owner;
	}

	@Override
	public DeclaredField<?> declareField(FieldDeclaration declaration) {
		if (declaration.isAbstract()
				&& !getOwner().isPrototype()
				&& !getOwner().isAbstract()) {
			getOwner().getLogger().prohibitedAbstract(
					declaration,
					declaration.getDisplayName());
			return null;
		}
		return declare(declaration);
	}

	@Override
	public void declareMember(Member member) {
		member.assertScopeIs(getOwner().getScope());
		this.members.add(member);
	}

	public void registerMembers(ObjectMembers members) {
		members.addMembers(this.members);
	}

	@Override
	public boolean declareBlock(LocationSpec location, String name) {
		if (!this.blockNames.add(name)) {
			location.getContext().getLogger().dublicateBlockName(
					location,
					name);
			return false;
		}
		return true;
	}

	@Override
	public String anonymousBlockName() {
		return Integer.toString(++this.localScopeIndex);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName());
		out.append('[');
		out.append(this.owner != null ? this.owner : "<unresolved>");
		out.append("]{");

		boolean comma = false;

		for (Object m : this.members) {
			if (comma) {
				out.append(", ");
			} else {
				comma = true;
			}
			out.append(m);
		}

		out.append('}');

		return out.toString();
	}

	private DeclaredField<?> declare(FieldDeclaration declaration) {
		assert getOwner() == declaration.getContainer().toObject() :
			"Wrong container " + declaration.getContainer()
			+ ", but " + getOwner() + " expected";

		final DeclaredField<?> field = DeclaredField.declareField(declaration);

		this.members.add(field.toMember());

		return field;
	}

}
