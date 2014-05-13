/*
    Abstract Syntax Tree
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.ast.statement;

import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclaratorNode;


public interface StatementNodeVisitor<R, P>
		extends ExpressionNodeVisitor<R, P> {

	default R visitBraces(BracesNode braces, P p) {
		return visitStatement(braces, p);
	}

	default R visitAssignment(AssignmentNode assignment, P p) {
		return visitStatement(assignment, p);
	}

	default R visitReturn(ReturnNode ret, P p) {
		return visitStatement(ret, p);
	}

	default R visitDeclarator(DeclaratorNode declarator, P p) {
		return visitStatement(declarator, p);
	}

	default R visitLocalScope(LocalScopeNode scope, P p) {
		return visitStatement(scope, p);
	}

	default R visitClauseDeclarator(ClauseDeclaratorNode declarator, P p) {
		return visitStatement(declarator, p);
	}

	default R visitNamedBlock(NamedBlockNode block, P p) {
		return visitStatement(block, p);
	}

	@Override
	default R visitExpression(ExpressionNode expression, P p) {
		return visitStatement(expression, p);
	}

	R visitStatement(StatementNode statement, P p);

}
