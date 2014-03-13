/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;

import static org.o42a.core.member.clause.impl.GroupRegistry.prohibitedContinuation;

import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.st.impl.imperative.ImperativeMemberRegistry;
import org.o42a.core.st.sentence.Statements;


final class ImperativeGroupRegistry extends ImperativeMemberRegistry {

	private final DeclaredGroupClause group;

	ImperativeGroupRegistry(
			DeclaredGroupClause group,
			MemberRegistry registry) {
		super(registry);
		this.group = group;
	}

	@Override
	public ClauseBuilder newClause(
			Statements statements,
			ClauseDeclaration declaration) {
		if (this.group.isTerminator()) {
			prohibitedContinuation(declaration);
			return null;
		}
		if (declaration.getKind() == ClauseKind.OVERRIDER) {
			declaration.getLogger().error(
					"prohibited_overrider_clause",
					declaration,
					"Overrider clause is prohibited here");
			return null;
		}
		return registry().newClause(
				statements,
				declaration.inGroup(getGroupId()));
	}

	private final MemberId getGroupId() {

		final MemberId memberId = this.group.getDeclaration().getMemberId();
		final MemberId[] ids = memberId.getIds();

		return ids[ids.length - 1];
	}

}
