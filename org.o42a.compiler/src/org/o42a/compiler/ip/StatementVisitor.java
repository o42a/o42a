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
package org.o42a.compiler.ip;

import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.sentence.Statements;


public abstract class StatementVisitor
		extends AbstractStatementVisitor<Void, Statements<?>> {

	private final Interpreter ip;
	private final CompilerContext context;

	public StatementVisitor(Interpreter ip, CompilerContext context) {
		this.ip = ip;
		this.context = context;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final ExpressionNodeVisitor<Ref, Distributor> expressionVisitor() {
		return ip().targetExVisitor();
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	protected Void visitStatement(StatementNode statement, Statements<?> p) {
		getLogger().invalidStatement(statement);
		return null;
	}

}
