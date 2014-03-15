/*
    Abstract Syntax Tree
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
package org.o42a.ast.statement;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;


public class PassThroughNode extends AbstractStatementNode {

	private final ExpressionNode input;
	private final SignNode<Operator> operator;
	private final RefNode flow;

	public PassThroughNode(
			ExpressionNode input,
			SignNode<Operator> operator,
			RefNode flow) {
		super(start(input, operator), end(operator, flow));
		this.input = input;
		this.operator = operator;
		this.flow = flow;
	}

	public final ExpressionNode getInput() {
		return this.input;
	}

	public final SignNode<Operator> getOperator() {
		return this.operator;
	}

	public final RefNode getFlow() {
		return this.flow;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPassThrough(this, p);
	}

	@Override
	public ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public ExpressionNode toExpression() {
		return null;
	}

	@Override
	public RefNode toRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.input != null) {
			this.input.printContent(out);
		}
		this.operator.printContent(out);
		if (this.flow != null) {
			this.flow.printContent(out);
		}
	}

	public enum Operator implements SignType {

		CHEVRON() {

			@Override
			public String getSign() {
				return ">>";
			}

		}

	}

}
