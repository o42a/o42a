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


public class BinaryNode
		extends AbstractExpressionNode
		implements ClauseIdNode {

	private final ExpressionNode leftOperand;
	private final SignNode<BinaryOperator> sign;
	private final ExpressionNode rightOperand;

	public BinaryNode(
			ExpressionNode leftOperand,
			SignNode<BinaryOperator> sign,
			ExpressionNode rightOperand) {
		super(
				leftOperand.getStart(),
				end(sign, rightOperand));
		this.leftOperand = leftOperand;
		this.sign = sign;
		this.rightOperand = rightOperand;
	}

	public ExpressionNode getLeftOperand() {
		return this.leftOperand;
	}

	public final BinaryOperator getOperator() {
		return this.sign.getType();
	}

	public SignNode<BinaryOperator> getSign() {
		return this.sign;
	}

	public ExpressionNode getRightOperand() {
		return this.rightOperand;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBinary(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBinary(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
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
		return this;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.leftOperand.printContent(out);
		out.append(' ');
		this.sign.printContent(out);
		if (this.rightOperand != null) {
			out.append(' ');
			this.rightOperand.printContent(out);
		} else {
			out.append(" ?");
		}
	}

}
