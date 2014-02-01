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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;


public class NamedBlockNode extends AbstractStatementNode {

	private final NameNode name;
	private final SignNode<Separator> separator;
	private final BracesNode block;

	public NamedBlockNode(
			NameNode name,
			SignNode<Separator> separator,
			BracesNode block) {
		super(name.getStart(), block.getEnd());
		this.name = name;
		this.separator = separator;
		this.block = block;
	}

	public final NameNode getName() {
		return this.name;
	}

	public final SignNode<Separator> getSeparator() {
		return this.separator;
	}

	public final BracesNode getBlock() {
		return this.block;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitNamedBlock(this, p);
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.name.printContent(out);
		this.separator.printContent(out);
		this.block.printContent(out);
	}

	public enum Separator implements SignType {

		COLON() {

			@Override
			public String getSign() {
				return ":";
			}

		}

	}

}
