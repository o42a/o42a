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
package org.o42a.core.member.field.decl;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;


public final class DeclaredMemberField extends MemberField {

	private final FieldBuilder builder;
	private FieldDeclarationStatement statement;

	public DeclaredMemberField(FieldBuilder builder) {
		super(builder.getMemberOwner(), builder.getDeclaration());
		this.builder = builder;
	}

	@Override
	public final MemberField getPropagatedFrom() {
		return null;
	}

	@Override
	public MemberField propagateTo(Obj owner) {
		return new OverriddenDeclaredMemberField(owner, this);
	}

	public final FieldDeclarationStatement getStatement() {
		return this.statement;
	}

	public final void setStatement(FieldDeclarationStatement statement) {
		this.statement = statement;
	}

	@Override
	protected Field createField() {
		return new DeclaredField(this, this.builder.getDefinition());
	}

	final DeclaredField toDeclaredField() {
		return (DeclaredField) toField().field(dummyUser());
	}

}
