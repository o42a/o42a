/*
    Abstract Syntax Tree
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.ast.phrase;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.HorizontalEllipsis;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.expression.ExpressionNode;


public final class IntervalNode
		extends AbstractNode
		implements PhrasePartNode, ClauseIdNode {

	private final SignNode<IntervalBracket> leftBracket;
	private final ExpressionNode leftBound;
	private final SignNode<HorizontalEllipsis> ellipsis;
	private final ExpressionNode rightBound;
	private final SignNode<IntervalBracket> rightBracket;

	public IntervalNode(
			SignNode<IntervalBracket> leftBracket,
			ExpressionNode leftBound,
			SignNode<HorizontalEllipsis> ellipsis,
			ExpressionNode rightBound,
			SignNode<IntervalBracket> rightBracket) {
		super(leftBracket.getStart(), end(ellipsis, rightBound, rightBracket));
		this.leftBracket = leftBracket;
		this.leftBound = leftBound;
		this.ellipsis = ellipsis;
		this.rightBound = rightBound;
		this.rightBracket = rightBracket;
	}

	public final SignNode<IntervalBracket> getLeftBracket() {
		return this.leftBracket;
	}

	public final ExpressionNode getLeftBound() {
		return this.leftBound;
	}

	public final SignNode<HorizontalEllipsis> getEllipsis() {
		return this.ellipsis;
	}

	public final ExpressionNode getRightBound() {
		return this.rightBound;
	}

	public final SignNode<IntervalBracket> getRightBracket() {
		return this.rightBracket;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitInterval(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitInterval(this, p);
	}

	@Override
	public <R, P> R accept(PhrasePartNodeVisitor<R, P> visitor, P p) {
		return visitor.visitInterval(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.leftBracket.printContent(out);
		if (this.leftBound != null) {
			this.leftBound.printContent(out);
		}
		this.ellipsis.printContent(out);
		if (this.rightBound != null) {
			this.rightBound.printContent(out);
		}
		if (this.rightBracket != null) {
			this.rightBracket.printContent(out);
		}
	}

}
