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
import org.o42a.core.member.alias.AliasDeclarationStatement;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.decl.DeclaredMemberField;
import org.o42a.core.member.field.decl.FieldDeclarationStatement;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;


public final class FieldBuilder implements ContainerInfo {

	private final MemberRegistry memberRegistry;
	private final FieldDeclaration declaration;
	private final FieldDefinition definition;
	private final Ref ref;

	public FieldBuilder(
			MemberRegistry memberRegistry,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
		this.definition = definition;
		this.ref = null;
		assert declaration.assertSameScope(definition);
	}

	public FieldBuilder(
			MemberRegistry memberRegistry,
			FieldDeclaration declaration,
			Ref ref) {
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
		this.definition = null;
		this.ref = ref;
		assert declaration.assertSameScope(ref);
	}

	public final Obj getMemberOwner() {
		return this.memberRegistry.getOwner();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final boolean isAlias() {
		return this.ref != null;
	}

	public final FieldDefinition getDefinition() {
		return this.definition;
	}

	public final Ref getRef() {
		return this.ref;
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
		if (isAlias()) {
			return declareAlias();
		}
		return declareField();
	}

	private DeclarationStatement declareField() {

		final DeclaredMemberField member = new DeclaredMemberField(this);

		this.memberRegistry.declareMember(member);

		final FieldDeclarationStatement statement =
				new FieldDeclarationStatement(this, member);

		return statement;
	}

	private DeclarationStatement declareAlias() {

		final MemberAlias member = new MemberAlias(this.memberRegistry, this);

		this.memberRegistry.declareMember(member);

		final AliasDeclarationStatement statement =
				new AliasDeclarationStatement(this, member);

		return statement;
	}

	@Override
	public String toString() {
		return "FieldBuilder[" + this.declaration + "]:"
				+ (this.definition != null ? this.definition : this.ref);
	}

}
