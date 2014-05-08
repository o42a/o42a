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
package org.o42a.core.member.alias;

import org.o42a.core.member.DeclarationCommand;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.decl.FieldDeclarationCommand;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;


public class AliasDeclarationStatement extends DeclarationStatement {

	private final FieldBuilder builder;
	private final MemberAlias member;

	public AliasDeclarationStatement(FieldBuilder builder, MemberAlias member) {
		super(builder, builder.distribute());
		this.builder = builder;
		this.member = member;
	}

	@Override
	public final MemberAlias toMember() {
		return this.member;
	}

	@Override
	public DeclarationCommand command(CommandEnv env) {
		return new FieldDeclarationCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final FieldDeclaration declaration =
				this.builder.getDeclaration().reproduce(reproducer);

		if (declaration == null) {
			return null;
		}

		final Ref ref = this.builder.getRef().reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		final FieldBuilder builder =
				reproducer.getStatements().alias(declaration, ref);

		if (builder == null) {
			return null;
		}

		return builder.build();
	}

	@Override
	public String toString() {
		return ("AliasDeclarationStatement["
				+ this.member + ":-"
				+ this.builder.getRef()) + ']';
	}

}
