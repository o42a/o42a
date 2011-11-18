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
package org.o42a.core.member.impl.local;

import org.o42a.core.member.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;


public final class PropagatedMemberLocal extends MemberLocal {

	private final MemberLocal propagatedFrom;
	private PropagatedLocalScope local;

	public PropagatedMemberLocal(
			MemberOwner owner,
			MemberLocal propagatedFrom) {
		super(owner, propagatedFrom);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public MemberId getId() {
		return this.propagatedFrom.getId();
	}

	@Override
	public MemberKey getKey() {
		return this.propagatedFrom.getKey();
	}

	@Override
	public Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public LocalScope toLocal() {
		return local();
	}

	private final PropagatedLocalScope local() {
		if (this.local != null) {
			return this.local;
		}
		return this.local = new PropagatedLocalScope(
				this,
				getPropagatedFrom().toLocal());
	}

}
