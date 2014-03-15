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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;


public class FlowNode extends AbstractStatementNode {

	private final NameNode name;
	private final SignNode<FlowOperator> operator;
	private final BracesNode block;

	public FlowNode(
			NameNode name,
			SignNode<FlowOperator> operator,
			BracesNode block) {
		super(name.getStart(), block.getEnd());
		this.name = name;
		this.operator = operator;
		this.block = block;
	}

	public final NameNode getName() {
		return this.name;
	}

	public final SignNode<FlowOperator> getOperator() {
		return this.operator;
	}

	public final BracesNode getBlock() {
		return this.block;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitFlow(this, p);
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
		this.name.printContent(out);
		this.operator.printContent(out);
		this.block.printContent(out);
	}

}
