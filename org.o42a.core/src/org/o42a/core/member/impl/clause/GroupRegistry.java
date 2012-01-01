/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.member.impl.clause;

import static org.o42a.core.member.Inclusions.noInclusions;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalScope;
import org.o42a.util.Lambda;


final class GroupRegistry
		extends MemberRegistry
		implements Lambda<MemberRegistry, LocalScope> {

	private final DeclaredGroupClause group;
	private final MemberRegistry registry;

	GroupRegistry(DeclaredGroupClause group, MemberRegistry registry) {
		super(noInclusions());
		this.group = group;
		this.registry = registry;
	}

	@Override
	public MemberOwner getMemberOwner() {
		return this.registry.getMemberOwner();
	}

	@Override
	public Obj getOwner() {
		return this.registry.getOwner();
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return this.registry.newField(
				declaration.inGroup(getGroupId()),
				definition);
	}

	@Override
	public ClauseBuilder newClause(ClauseDeclaration declaration) {
		if (this.group.isTerminator()) {
			prohibitedContinuation(declaration);
			return null;
		}
		return this.registry.newClause(declaration.inGroup(getGroupId()));
	}

	@Override
	public void declareMember(Member member) {
		this.registry.declareMember(member);
	}

	@Override
	public String anonymousBlockName() {
		return this.registry.anonymousBlockName();
	}

	@Override
	public MemberRegistry get(LocalScope arg) {
		return this;
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
