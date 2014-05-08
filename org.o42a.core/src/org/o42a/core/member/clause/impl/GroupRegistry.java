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

import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;

import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.ProxyMemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Statements;


final class GroupRegistry
		extends ProxyMemberRegistry {

	private final DeclaredGroupClause group;

	GroupRegistry(DeclaredGroupClause group, MemberRegistry registry) {
		super(NO_INCLUSIONS, registry);
		this.group = group;
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return registry().newField(
				declaration.inGroup(getGroupId()),
				definition);
	}

	@Override
	public FieldBuilder newAlias(FieldDeclaration declaration, Ref ref) {
		return registry().newAlias(
				declaration.inGroup(getGroupId()),
				ref);
	}

	@Override
	public ClauseBuilder newClause(
			Statements statements,
			ClauseDeclaration declaration) {
		if (this.group.isTerminator()) {
			prohibitedContinuation(declaration);
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

	static void prohibitedContinuation(ClauseDeclaration declaration) {
		declaration.getLogger().error(
				"prohibited_terminator_clause_continuation",
				declaration,
				"Terminator may not have continuations");
	}

}
