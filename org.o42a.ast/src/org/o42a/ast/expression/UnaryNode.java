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
package org.o42a.ast.expression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentNode;


public class UnaryNode
		extends AbstractExpressionNode
		implements ClauseIdNode {

	private final SignNode<UnaryOperator> sign;
	private final ExpressionNode operand;

	public UnaryNode(SignNode<UnaryOperator> sign, ExpressionNode operand) {
		super(sign.getStart(), end(sign, operand));
		this.sign = sign;
		this.operand = operand;
	}

	public final UnaryOperator getOperator() {
		return this.sign.getType();
	}

	public SignNode<UnaryOperator> getSign() {
		return this.sign;
	}

	public ExpressionNode getOperand() {
		return this.operand;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitUnary(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitUnary(this, p);
	}

	@Override
	public DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
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
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.sign.printContent(out);
		this.operand.printContent(out);
	}

}
