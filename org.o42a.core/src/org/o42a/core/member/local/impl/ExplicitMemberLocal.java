/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.member.local.impl;

import static org.o42a.core.member.MemberName.localName;

import org.o42a.core.Distributor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.object.OwningObject;
import org.o42a.core.source.LocationInfo;


final class ExplicitMemberLocal extends MemberLocal {

	private LocalScope local;
	private MemberId id;
	private MemberKey key;

	ExplicitMemberLocal(
			LocationInfo localation,
			Distributor distributor,
			OwningObject owner) {
		super(localation, distributor, owner);
	}

	@Override
	public final MemberId getMemberId() {
		return this.id;
	}

	@Override
	public final MemberKey getMemberKey() {
		return this.key;
	}

	@Override
	public Member getPropagatedFrom() {
		return null;
	}

	@Override
	public LocalScope local() {
		return this.local;
	}

	final void init(ExplicitLocalScope local) {
		this.local = local;

		final MemberId localId = localName(local.getName());
		final Member member = getContainer().toMember();

		if (member != null
				&& member.getContainer().getScope().is(getScope())) {
			this.id = member.getMemberId().append(localId);
		} else {

			assert getContainer().toObject().is(local.getOwner()) :
				"Wrong local scope container: " + getContainer();

			this.id = localId;
		}

		this.key = this.id.key(getScope());
	}

	final void initReproduced(
			ExplicitLocalScope local,
			LocalScope reproducedFrom) {
		this.local = local;
		this.id =
				reproducedFrom.toMember().getMemberKey().getMemberId()
				.reproduceFrom(reproducedFrom);
		this.key = this.id.key(getScope());
	}

}
