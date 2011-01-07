/*
    Abstract Syntax Tree
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
package org.o42a.ast.statement;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.ExpressionNode;


public class AssignmentNode extends AbstractStatementNode {

	private final ExpressionNode destination;
	private final SignNode<AssignmentOperator> operator;
	private final ExpressionNode value;

	public AssignmentNode(
			ExpressionNode destination,
			SignNode<AssignmentOperator> operator,
			ExpressionNode value) {
		super(
				destination.getStart(),
				end(operator, value));
		this.destination = destination;
		this.operator = operator;
		this.value = value;
	}

	public ExpressionNode getDestination() {
		return this.destination;
	}

	public SignNode<AssignmentOperator> getOperator() {
		return this.operator;
	}

	public ExpressionNode getValue() {
		return this.value;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAssignment(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.destination.printContent(out);
		this.operator.printContent(out);
		if (this.value != null) {
			this.value.printContent(out);
		}
	}

	public enum AssignmentOperator implements SignType {

		ASSIGN;

		@Override
		public String getSign() {
			return "=";
		}

	}

}
