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

import org.o42a.core.member.*;
import org.o42a.core.member.local.MemberLocalScope;


public final class PropagatedMemberLocalScope extends MemberLocalScope {

	private final MemberLocalScope propagatedFrom;
	private PropagatedLocalScope local;

	public PropagatedMemberLocalScope(
			MemberOwner owner,
			MemberLocalScope propagatedFrom) {
		super(owner, propagatedFrom);
		this.propagatedFrom = propagatedFrom;
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
	public Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public final PropagatedLocalScope localScope() {
		if (this.local != null) {
			return this.local;
		}
		return this.local = new PropagatedLocalScope(
				this,
				getPropagatedFrom().toLocalScope().localScope());
	}

}
