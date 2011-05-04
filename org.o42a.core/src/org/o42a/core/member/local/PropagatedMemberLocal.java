/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.member.local;

import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.util.use.UserInfo;


final class PropagatedMemberLocal extends MemberLocal {

	private final PropagatedLocalScope localScope;
	private final LocalScope overridden;

	PropagatedMemberLocal(
			PropagatedLocalScope localScope,
			LocalScope overridden) {
		super(
				localScope,
				localScope.getOwner().distribute(),
				localScope.getOwner().toMemberOwner());
		this.localScope = localScope;
		this.overridden = overridden;
	}

	@Override
	public MemberId getId() {
		return this.localScope.explicit.toMember().getId();
	}

	@Override
	public MemberKey getKey() {
		return this.localScope.explicit.toMember().getKey();
	}

	@Override
	public Member getPropagatedFrom() {
		return this.overridden.toMember();
	}

	@Override
	public LocalScope toLocal(UserInfo user) {
		useBy(user);
		return this.localScope;
	}

	@Override
	protected void useBy(UserInfo user) {
		super.useBy(user);
		useSubstanceBy(this.localScope.newResolver(user));
	}

}
