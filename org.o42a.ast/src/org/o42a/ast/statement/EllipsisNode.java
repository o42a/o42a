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

import org.o42a.ast.atom.HorizontalEllipsis;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;


public class EllipsisNode extends AbstractStatementNode {

	private final SignNode<HorizontalEllipsis> mark;
	private final NameNode target;

	public EllipsisNode(SignNode<HorizontalEllipsis> mark, NameNode target) {
		super(mark.getStart(), end(mark, target));
		this.mark = mark;
		this.target = target;
	}

	public final SignNode<HorizontalEllipsis> getMark() {
		return this.mark;
	}

	public final NameNode getTarget() {
		return this.target;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitEllipsis(this, p);
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
		this.mark.printContent(out);
		if (this.target != null) {
			this.target.printContent(out);
		}
	}
}
