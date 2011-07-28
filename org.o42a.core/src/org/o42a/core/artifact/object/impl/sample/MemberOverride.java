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
package org.o42a.core.artifact.object.impl.sample;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.*;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;


public final class MemberOverride extends Sample {

	private final Member overriddenMember;
	private final TypeRef ancestor;
	private final StaticTypeRef typeRef;

	public MemberOverride(Member overriddenMember, Ascendants ascendants) {
		super(overriddenMember, ascendants);
		this.overriddenMember = overriddenMember;
		this.ancestor =
				getObject().type().getAncestor().upgradeScope(getScope());
		this.typeRef =
				getObject().fixedRef(getScope().distribute()).toStaticTypeRef();
	}

	@Override
	public TypeRef getAncestor() {
		return this.ancestor;
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public boolean isExplicit() {
		return false;
	}

	@Override
	public Member getOverriddenMember() {
		return this.overriddenMember;
	}

	@Override
	public StaticTypeRef getExplicitAscendant() {
		return null;
	}

	@Override
	public void reproduce(
			Reproducer reproducer,
			AscendantsBuilder<?> ascendants) {
		// Will be done automatically.
	}

	@Override
	public String toString() {
		return "MemberOverride[" + this.overriddenMember + ']';
	}

	@Override
	protected final Obj getObject() {
		return this.overriddenMember.substance(dummyUser()).toObject();
	}

}
