/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


public abstract class ProxyMemberRegistry extends MemberRegistry {

	private final MemberRegistry registry;

	public ProxyMemberRegistry(MemberRegistry registry) {
		super(registry.inclusions());
		this.registry = registry;
	}

	public ProxyMemberRegistry(
			Inclusions inclusions,
			MemberRegistry registry) {
		super(inclusions);
		this.registry = registry;
	}

	@Override
	public Obj getOwner() {
		return registry().getOwner();
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		return registry().newField(declaration, definition);
	}

	@Override
	public ClauseBuilder newClause(
			Statements<?, ?> statements,
			ClauseDeclaration declaration) {
		return registry().newClause(statements, declaration);
	}

	@Override
	public void declareMember(Member member) {
		registry().declareMember(member);
	}

	@Override
	public MemberId tempMemberId() {
		return registry().tempMemberId();
	}

	@Override
	public Name anonymousBlockName() {
		return registry().anonymousBlockName();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.registry + ']';
	}

	protected final MemberRegistry registry() {
		return this.registry;
	}

}
