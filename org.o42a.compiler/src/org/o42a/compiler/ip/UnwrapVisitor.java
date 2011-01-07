/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.unwrap;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.StatementNode;


final class UnwrapVisitor
		extends AbstractStatementVisitor<ExpressionNode, Void> {

	static final UnwrapVisitor UNWRAP_VISITOR = new UnwrapVisitor();

	private UnwrapVisitor() {
	}

	@Override
	public ExpressionNode visitParentheses(
			ParenthesesNode parentheses,
			Void p) {
		return unwrap(parentheses);
	}

	@Override
	protected ExpressionNode visitExpression(
			ExpressionNode expression,
			Void p) {
		return expression;
	}

	@Override
	protected ExpressionNode visitStatement(
			StatementNode statement,
			Void p) {
		return null;
	}

}
