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
package org.o42a.core.object.common;

import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import java.util.ArrayList;

import org.o42a.core.member.*;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.ref.Ref;
import org.o42a.util.string.Name;


public class ObjectMemberRegistry extends MemberRegistry {

	private Obj owner;
	private int tempMemberIndex;
	private int localScopeIndex;

	private final ArrayList<Member> pending = new ArrayList<>();
	private ObjectMembers members;

	public ObjectMemberRegistry(Inclusions inclusions, Obj owner) {
		super(inclusions);
		if (owner == null) {
			throw new NullPointerException("Owner not specified");
		}
		this.owner = owner;
	}

	protected ObjectMemberRegistry(Inclusions inclusions) {
		super(inclusions);
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
					declaration.getLocation(),
					declaration.getDisplayName());
			return null;
		}

		assert getOwner().is(declaration.getContainer().toObject()) :
			"Wrong container " + declaration.getContainer()
			+ ", but " + getOwner() + " expected";

		return new FieldBuilder(this, declaration, definition);
	}

	@Override
	public FieldBuilder newAlias(FieldDeclaration declaration, Ref ref) {
		assert getOwner().is(declaration.getContainer().toObject()) :
			"Wrong container " + declaration.getContainer()
			+ ", but " + getOwner() + " expected";
		return new FieldBuilder(this, declaration, ref);
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
	public MemberId tempMemberId(MemberIdKind kind) {
		return kind.memberName(CASE_SENSITIVE.canonicalName(
				Integer.toString(++this.tempMemberIndex)));
	}

	@Override
	public Name anonymousBlockName() {
		return CASE_SENSITIVE.canonicalName(
				Integer.toString(++this.localScopeIndex));
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

	protected final Obj setOwner(Obj owner) {
		return this.owner = owner;
	}

}
