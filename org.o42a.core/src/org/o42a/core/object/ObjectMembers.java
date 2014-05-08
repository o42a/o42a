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
package org.o42a.core.object;

import java.util.Map;

import org.o42a.core.member.*;


public class ObjectMembers extends ContainerMembers {

	private int idSeq;

	ObjectMembers(Obj owner) {
		super(owner);
	}

	public final int nextId() {
		return ++this.idSeq;
	}

	public final void addTypeParameter(Member typeParameter) {
		assert typeParameter.isTypeParameter() :
			"Not a type parameter: " + typeParameter;
		assert !typeParameter.isOverride() :
			"Can not override the type parameter " + typeParameter;
		add(typeParameter, false);
	}

	public void deriveMembers(Obj ascendant) {
		for (Member member : ascendant.getMembers()) {
			propagateMember(member);
		}
	}

	@Override
	protected final Map<MemberKey, Member> members() {
		return getOwner().members();
	}

	@Override
	protected final Map<MemberId, Symbol> symbols() {
		return getOwner().symbols();
	}

}
