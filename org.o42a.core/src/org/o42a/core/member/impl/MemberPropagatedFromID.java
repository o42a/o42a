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
package org.o42a.core.member.impl;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public class MemberPropagatedFromID implements SubID {

	public static ID memberScopePrefix(Member member) {

		final Scope scope = member.getScope();

		if (scope.is(member.getContext().getRoot().getScope())) {
			return ID.topId();
		}

		return scope.getId();
	}

	private static final SubID PROPAGATED_FROM =
			ID.displayText(" {propagated from: ");
	private static final ID DECLARED = ID.id(ID.displayText(""));
	private static final SubID CLONE_OF =
			ID.displayText(" {clone of: ");
	private static final SubID SUFFIX = ID.displayText("}");
	private static final SubID COMMA = ID.displayText(", ");

	private final Member member;

	public MemberPropagatedFromID(Member member) {
		this.member = member;
	}

	@Override
	public ID toID() {
		return ID.id().in(this.member.getMemberKey().getOrigin().getId());
	}

	@Override
	public ID toDisplayID() {
		if (this.member.isClone()) {

			final Member lastDefinition = this.member.getLastDefinition();

			return ID.id(CLONE_OF).suffix(
					memberScopePrefix(lastDefinition)
					.sub(lastDefinition.getMemberId())
					.suffix(SUFFIX));
		}

		final Member[] allOverridden = this.member.getOverridden();

		if (allOverridden.length == 0) {
			return DECLARED;
		}

		boolean comma = false;
		ID id = ID.id(PROPAGATED_FROM);

		for (Member overridden : allOverridden) {
			if (!comma) {
				comma = true;
			} else {
				id = id.suffix(COMMA);
			}
			id = id.suffix(
					memberScopePrefix(overridden)
					.sub(overridden.getMemberId()));
		}

		return id.suffix(SUFFIX);
	}

}
