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
import org.o42a.core.def.Rescoper;
import org.o42a.core.member.Member;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;


public final class MemberOverride extends Sample {

	private final Member overriddenMember;
	private final TypeRef ancestor;
	private final StaticTypeRef typeRef;

	public MemberOverride(Member overriddenMember, Ascendants ascendants) {
		super(overriddenMember, ascendants);
		this.overriddenMember = overriddenMember;
		this.ancestor = ancestor();

		final Obj object = getObject();
		final StaticTypeRef typeRef =
				object.selfRef()
				.toStaticTypeRef()
				.rescope(object.getScope().getEnclosingScope());

		this.typeRef = typeRef.upgradeScope(getScope());
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

	private TypeRef ancestor() {

		final Obj object = getObject();
		final TypeRef ancestor = object.type().getAncestor();

		return ancestor.setValueStruct(valueStruct()).upgradeScope(getScope());
	}

	private ValueStruct<?, ?> valueStruct() {

		final Obj object = getObject();
		final Path memberPath =
				this.overriddenMember.getKey().toPath().materialize();
		final Rescoper rescoper = memberPath.bind(
				this,
				object.getScope().getEnclosingScope()).rescoper();

		return object.value().getValueStruct().rescope(rescoper);
	}

}
