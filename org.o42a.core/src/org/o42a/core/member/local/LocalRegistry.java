/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberRegistry;


public class LocalRegistry extends MemberRegistry {

	private final LocalScope scope;
	private final MemberRegistry ownerRegistry;

	public LocalRegistry(LocalScope scope, MemberRegistry ownerRegistry) {
		this.scope = scope;
		this.ownerRegistry = ownerRegistry;
	}

	@Override
	public Obj getOwner() {
		return null;
	}

	public final LocalScope getScope() {
		return this.scope;
	}

	@Override
	public DeclaredField<?> declareField(FieldDeclaration declaration) {
		assert declaration.getPlace().isImperative() :
			"Imperative field declaration expected: " + declaration;

		final DeclaredField<?> field = DeclaredField.declareField(declaration);

		if (this.scope.addMember(field.toMember())) {
			return field;
		}

		return null;
	}

	@Override
	public ClauseBuilder newClause(ClauseDeclaration declaration) {
		declaration.getLogger().prohibitedClauseDeclaration(declaration);
		return null;
	}

	@Override
	public void declareMember(Member member) {
		member.assertScopeIs(this.scope);
		this.scope.addMember(member);
	}

	@Override
	public boolean declareBlock(LocationSpec location, String name) {
		return this.ownerRegistry.declareBlock(location, name);
	}

	@Override
	public String anonymousBlockName() {
		return this.ownerRegistry.anonymousBlockName();
	}

}
