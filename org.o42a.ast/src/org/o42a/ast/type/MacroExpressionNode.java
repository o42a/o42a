/*
    Abstract Syntax Tree
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.ast.type;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.NoBoundNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class MacroExpressionNode extends AbstractNode implements TypeNode {

	private final ExpressionNode expression;

	public MacroExpressionNode(ExpressionNode expression) {
		super(expression.getStart(), expression.getEnd());
		this.expression = expression;
	}

	public final ExpressionNode getExpression() {
		return this.expression;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitMacroExpression(this, p);
	}

	@Override
	public <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMacroExpression(this, p);
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return getExpression().accept(visitor, p);
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return getExpression().accept(visitor, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final TypeNode toType() {
		return this;
	}

	@Override
	public final TypeArgumentNode toTypeArgument() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final LocalNode toLocal() {
		return null;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public final NoBoundNode toNoBound() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.expression.printContent(out);
	}

}
