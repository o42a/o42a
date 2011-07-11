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

import org.o42a.core.member.Inclusions;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;


public class ObjectMemberRegistry extends MemberRegistry {

	private int localScopeIndex;
	Obj owner;

	private final ArrayList<Member> pending = new ArrayList<Member>();
	private ObjectMembers members;

	public ObjectMemberRegistry(Inclusions inclusions, Obj owner) {
		super(inclusions);
		if (owner == null) {
			throw new NullPointerException("Owner not specified");
		}
		this.owner = owner;
	}

	ObjectMemberRegistry(Inclusions inclusions) {
		super(inclusions);
	}

	@Override
	public OwningObject getMemberOwner() {
		return getOwner().toMemberOwner();
	}

	@Override
	public Obj getOwner() {
		return this.owner;
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (declaration.isAbstract()
				&& !getOwner().isPrototype()
				&& !getOwner().isAbstract()) {
			getOwner().getLogger().prohibitedAbstract(
					declaration,
					declaration.getDisplayName());
			return null;
		}

		assert getOwner() == declaration.getContainer().toObject() :
			"Wrong container " + declaration.getContainer()
			+ ", but " + getOwner() + " expected";

		return createFieldBuilder(declaration, definition);
	}

	@Override
	public void declareMember(Member member) {
		member.assertScopeIs(getOwner().getScope());
		if (this.members != null) {
			this.members.addMember(member);
		} else {
			this.pending.add(member);
		}
	}

	public void registerMembers(ObjectMembers members) {
		assert this.members == null :
			"Object members already registered: " + this;
		this.members = members;
		for (Member member : this.pending) {
			members.addMember(member);
		}
		this.pending.clear();
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
		if (this.members != null) {
			out.append(this.members);
		} else {
			out.append("pending: ");

			boolean comma = false;

			for (Object m : this.pending) {
				if (comma) {
					out.append(", ");
				} else {
					comma = true;
				}
				out.append(m);
			}
		}
		out.append('}');

		return out.toString();
	}

}
