/*
    Abstract Syntax Tree
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.ast.atom;

import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;


public class NumberNode
		extends AbstractExpressionNode
		implements AtomNode, PhrasePartNode {

	private final SignNode<SignOfNumber> sign;
	private final DigitsNode integer;

	public NumberNode(SignNode<SignOfNumber> sign, DigitsNode integer) {
		super(sign, integer);
		this.sign = sign;
		this.integer = integer;
	}

	public final SignNode<SignOfNumber> getSign() {
		return this.sign;
	}

	public final boolean isNegative() {

		final SignNode<SignOfNumber> sign = getSign();

		return sign != null && sign.getType().isNegative();
	}

	public final DigitsNode getInteger() {
		return this.integer;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitNumber(this, p);
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitNumber(this, p);
	}

	@Override
	public <R, P> R accept(PhrasePartNodeVisitor<R, P> visitor, P p) {
		return visitor.visitNumber(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.sign != null) {
			this.sign.printContent(out);
		}
		this.integer.printContent(out);
	}

}
