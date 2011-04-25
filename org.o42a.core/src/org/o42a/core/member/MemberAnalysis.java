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
package org.o42a.core.member;

import static org.o42a.util.use.Usable.simpleUsable;

import org.o42a.core.artifact.object.Obj;
import org.o42a.util.use.Usable;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UserInfo;


public class MemberAnalysis {

	private final Member member;
	private final Usable<Member> user;

	MemberAnalysis(Member member) {
		this.member = member;
		this.user = simpleUsable(member);
		if (member.isOverride()) {
			// Member declaration should be used when overridden member used.
			getDeclarationAnalysis().user.useBy(this.user);
		}
	}

	public final Member getMember() {
		return this.member;
	}

	public final MemberAnalysis getDeclarationAnalysis() {
		if (!getMember().isOverride()) {
			return this;
		}

		final MemberKey memberKey = this.member.getKey();
		final Obj origin = memberKey.getOrigin().getContainer().toObject();
		final Member declaration = origin.member(memberKey);

		return declaration.getAnalysis();
	}

	public final boolean accessedBy(UseCase useCase) {
		return this.user.isUsedBy(useCase);
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return "MemberAnalysis[" + this.member + ']';
	}

	final void usedBy(UserInfo user) {
		this.user.useBy(user);
	}

}
