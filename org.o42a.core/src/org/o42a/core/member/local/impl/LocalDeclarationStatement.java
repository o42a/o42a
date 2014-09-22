/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member.local.impl;

import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.st.*;


public class LocalDeclarationStatement extends DeclarationStatement {

	private final MemberLocal member;

	public LocalDeclarationStatement(FieldBuilder builder, MemberLocal member) {
		super(builder, builder.distribute());
		this.member = member;
	}

	@Override
	public final MemberLocal toMember() {
		return this.member;
	}

	@Override
	public Command command(CommandEnv env) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		return null;
	}

	@Override
	public String toString() {
		return "LocalDeclarationStatement[" + this.member + ']';
	}

}
