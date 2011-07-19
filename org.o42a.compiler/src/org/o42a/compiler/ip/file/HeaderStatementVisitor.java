/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.file.HeaderStatement.notDirective;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.StatementVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.sentence.Statements;


final class HeaderStatementVisitor extends StatementVisitor {

	public HeaderStatementVisitor(CompilerContext context) {
		super(PLAIN_IP, context);
	}

	@Override
	protected Void visitExpression(ExpressionNode expression, Statements<?> p) {

		final Distributor distributor = p.nextDistributor();
		final Ref ref = expression.accept(expressionVisitor(), distributor);

		if (ref != null) {
			p.statement(new HeaderStatement(ref));
		}

		return null;
	}

	@Override
	protected Void visitStatement(StatementNode statement, Statements<?> p) {
		notDirective(getLogger(), statement);
		return null;
	}

}
