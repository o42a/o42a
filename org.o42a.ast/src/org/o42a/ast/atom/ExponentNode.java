/*
    Abstract Syntax Tree
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;


public class ExponentNode extends AbstractNode {

	private final SignNode<ExponentSymbol> symbol;
	private final SignNode<SignOfNumber> sign;
	private final DigitsNode digits;

	public ExponentNode(
			SignNode<ExponentSymbol> symbol,
			SignNode<SignOfNumber> sign,
			DigitsNode digits) {
		super(symbol.getStart(), digits.getEnd());
		this.symbol = symbol;
		this.sign = sign;
		this.digits = digits;
	}

	public final SignNode<ExponentSymbol> getSymbol() {
		return this.symbol;
	}

	public final SignNode<SignOfNumber> getSign() {
		return this.sign;
	}

	public final DigitsNode getDigits() {
		return this.digits;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitExponent(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.symbol.printContent(out);
		if (this.sign != null) {
			this.sign.printContent(out);
		}
		this.digits.printContent(out);
	}

}
