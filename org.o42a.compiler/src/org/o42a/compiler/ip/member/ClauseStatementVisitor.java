/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.member.ClauseInterpreter.buildOverrider;
import static org.o42a.core.member.clause.ClauseDeclaration.anonymousClauseDeclaration;

import org.o42a.ast.field.DeclaratorNode;
import org.o42a.compiler.ip.st.DefaultStatementVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Statements;


final class ClauseStatementVisitor extends DefaultStatementVisitor {

	ClauseStatementVisitor(CompilerContext context) {
		super(CLAUSE_DEF_IP, context);
	}

	@Override
	public Void visitDeclarator(DeclaratorNode declarator, Statements<?, ?> p) {
		if (!declarator.getTarget().isOverride()) {
			return super.visitDeclarator(declarator, p);
		}

		final Distributor distributor =
				new Placed(getContext(), declarator, p.nextDistributor())
				.distribute();
		final ClauseDeclaration declaration = anonymousClauseDeclaration(
				new Location(getContext(), declarator.getDeclarable()),
				distributor)
				.setKind(ClauseKind.OVERRIDER);
		final ClauseBuilder builder =
				buildOverrider(declaration, declarator, p);

		if (builder != null) {
			builder.mandatory().build();
		}

		return null;
	}

}
