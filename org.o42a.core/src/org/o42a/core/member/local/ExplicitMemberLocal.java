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

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.core.Distributor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.util.use.UserInfo;


final class ExplicitMemberLocal extends MemberLocal {

	private final LocalScope localScope;
	private final MemberId id;
	private final MemberKey key;

	ExplicitMemberLocal(LocalScope localScope, Distributor distributor) {
		super(localScope, distributor, localScope.getOwner().toMemberOwner());
		this.localScope = localScope;

		final MemberId localId =
			memberName("_local_" + this.localScope.getName());
		final Member member = getContainer().toMember();

		if (member != null
				&& member.getContainer().getScope() == getScope()) {
			this.id = member.getId().append(localId);
		} else {

			assert getContainer().toObject() == this.localScope.getOwner() :
				"Wrong local scope container: " + getContainer();

			this.id = localId;
		}

		this.key = this.id.key(getScope());
	}

	ExplicitMemberLocal(
			LocalScope localScope,
			Distributor distributor,
			LocalScope reproducedFrom) {
		super(localScope, distributor, localScope.getOwner().toMemberOwner());
		this.localScope = localScope;
		this.id =
			reproducedFrom.toMember().getKey().getMemberId()
			.reproduceFrom(reproducedFrom);
		this.key = this.id.key(getScope());
	}

	@Override
	public final MemberId getId() {
		return this.id;
	}

	@Override
	public final MemberKey getKey() {
		return this.key;
	}

	@Override
	public Member getPropagatedFrom() {
		return null;
	}

	@Override
	public LocalScope toLocal(UserInfo user) {
		useBy(user);
		return this.localScope;
	}

	@Override
	protected void useBy(UserInfo user) {
		super.useBy(user);
		this.localScope.newResolver(user);
	}

}
