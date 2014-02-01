/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.member.type.impl;

import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.value.TypeParameter;


public final class DeclaredMemberTypeParameter extends MemberTypeParameter {

	private final TypeParameter typeParameter;

	public DeclaredMemberTypeParameter(
			TypeParameter typeParameter,
			Obj owner) {
		super(typeParameter, owner.distribute(), owner);
		assert typeParameter.getScope().is(owner.getScope()) :
			"Attempt to declare the type parameter of "
			+ typeParameter.getScope() + " in the wrong object " + owner;
		this.typeParameter = typeParameter;
	}

	@Override
	public final MemberTypeParameter getPropagatedFrom() {
		return null;
	}

	@Override
	public final MemberId getMemberId() {
		return getMemberKey().getMemberId();
	}

	@Override
	public final MemberKey getMemberKey() {
		return this.typeParameter.getKey();
	}

	@Override
	public final boolean isOverride() {
		return false;
	}

}
