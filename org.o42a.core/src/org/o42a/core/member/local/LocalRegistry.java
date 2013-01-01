/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.member.Inclusions.noInclusions;

import org.o42a.core.member.*;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


public class LocalRegistry extends MemberRegistry {

	private final LocalScope scope;
	private final MemberRegistry ownerRegistry;

	public LocalRegistry(LocalScope scope, MemberRegistry ownerRegistry) {
		super(noInclusions());
		this.scope = scope;
		this.ownerRegistry = ownerRegistry;
	}

	@Override
	public final MemberOwner getMemberOwner() {
		return this.scope.toOwner();
	}

	@Override
	public final Obj getOwner() {
		return null;
	}

	public final LocalScope getScope() {
		return this.scope;
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		assert declaration.getPlace().isImperative() :
			"Imperative field declaration expected: " + declaration;
		return new FieldBuilder(this, declaration, definition);
	}

	@Override
	public ClauseBuilder newClause(
			Statements<?, ?> statements,
			ClauseDeclaration declaration) {
		declaration.getLogger()
		.prohibitedClauseDeclaration(declaration.getLocation());
		return null;
	}

	@Override
	public void declareMember(Member member) {
		member.assertScopeIs(this.scope);
		this.scope.addMember(member);
	}

	@Override
	public MemberId tempMemberId() {
		return this.ownerRegistry.tempMemberId();
	}

	@Override
	public Name anonymousBlockName() {
		return this.ownerRegistry.anonymousBlockName();
	}

}
