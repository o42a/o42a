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


public final class OverriddenMemberTypeParameter extends MemberTypeParameter {

	private final MemberTypeParameter propagatedFrom;

	public OverriddenMemberTypeParameter(
			Obj owner,
			MemberTypeParameter propagatedFrom) {
		super(
				owner.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				propagatedFrom.distributeIn(owner.getContainer()),
				owner);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public MemberTypeParameter getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public MemberId getMemberId() {
		return this.propagatedFrom.getMemberId();
	}

	@Override
	public MemberKey getMemberKey() {
		return this.propagatedFrom.getMemberKey();
	}

	@Override
	public boolean isOverride() {
		return true;
	}

}
