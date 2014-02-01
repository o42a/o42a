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
package org.o42a.core.member.field;

import org.o42a.core.*;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.decl.DeclaredMemberField;
import org.o42a.core.member.field.decl.FieldDeclarationStatement;
import org.o42a.core.object.Obj;
import org.o42a.core.source.Location;


public final class FieldBuilder implements ContainerInfo {

	private final MemberRegistry memberRegistry;
	private final FieldDeclaration declaration;
	private final FieldDefinition definition;

	public FieldBuilder(
			MemberRegistry memberRegistry,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
		this.definition = definition;
		declaration.assertSameScope(definition);
	}

	public final Obj getMemberOwner() {
		return this.memberRegistry.getOwner();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final FieldDefinition getDefinition() {
		return this.definition;
	}

	@Override
	public final Location getLocation() {
		return this.declaration.getLocation();
	}

	@Override
	public final Scope getScope() {
		return this.declaration.getScope();
	}

	@Override
	public final Container getContainer() {
		return this.declaration.getContainer();
	}

	@Override
	public final Distributor distribute() {
		return this.declaration.distribute();
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return this.declaration.distributeIn(container);
	}

	public DeclarationStatement build() {

		final DeclaredMemberField member = new DeclaredMemberField(this);
		final FieldDeclarationStatement statement =
				new FieldDeclarationStatement(this, member);

		member.setStatement(statement);
		this.memberRegistry.declareMember(member);

		return statement;
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		return "FieldBuilder[" + this.declaration + "]:" + this.definition;
	}

}
