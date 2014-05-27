/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import org.o42a.core.Scope;


public final class MemberRef {

	private final Scope owner;
	private final MemberKey memberKey;
	private Member member;

	public MemberRef(Scope owner, MemberKey memberKey) {
		assert owner != null :
			"Member owner not specified";
		assert memberKey != null :
			"Member key not specified";
		this.owner = owner;
		this.memberKey = memberKey;
	}

	public MemberRef(Scope owner, Member member) {
		assert owner != null :
			"Member owner not specified";
		assert member != null :
			"Member not specified";
		this.owner = owner;
		this.memberKey = member.getMemberKey();
		this.member = member;
	}

	public final Scope getOwner() {
		return this.owner;
	}

	public final MemberRef setOwner(Scope owner) {
		if (owner.is(getOwner())) {
			return this;
		}
		return new MemberRef(owner, getMemberKey());
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	public final Member getMember() {
		if (this.member != null) {
			return this.member;
		}
		return this.member = getOwner().getContainer().member(getMemberKey());
	}

	@Override
	public String toString() {
		if (this.memberKey == null) {
			return super.toString();
		}
		return this.memberKey.toString();
	}

}
