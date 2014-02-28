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
package org.o42a.core.member.field;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberKey.brokenMemberKey;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;


public final class FieldKey implements Nesting {

	static final FieldKey BROKEN_FIELD_KEY = new FieldKey(brokenMemberKey());

	private MemberKey memberKey;

	FieldKey(MemberKey memberKey) {
		this.memberKey = memberKey;
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	@Override
	public Obj findObjectIn(Scope enclosing) {

		final Member member = enclosing.getContainer().member(this.memberKey);

		assert member != null :
			this.memberKey + " not found in " + enclosing;

		return member.toField().object(dummyUser());
	}

	@Override
	public String toString() {
		if (this.memberKey == null) {
			return super.toString();
		}
		return this.memberKey.toString();
	}

}
