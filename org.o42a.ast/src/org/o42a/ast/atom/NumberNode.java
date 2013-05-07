/*
    Abstract Syntax Tree
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.ast.atom.Radix.DECIMAL_RADIX;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentNode;
import org.o42a.ast.type.TypeNode;


public class NumberNode
		extends AbstractExpressionNode
		implements AtomNode, PhrasePartNode {

	private final SignNode<SignOfNumber> sign;
	private final SignNode<Radix> radixPrefix;
	private final DigitsNode integer;
	private final FractionalPartNode fractional;
	private final ExponentNode exponent;

	public NumberNode(
			SignNode<SignOfNumber> sign,
			SignNode<Radix> radixPrefix,
			DigitsNode integer,
			FractionalPartNode fractional,
			ExponentNode exponent) {
		super(sign, radixPrefix, integer);
		this.sign = sign;
		this.radixPrefix = radixPrefix;
		this.integer = integer;
		this.fractional = fractional;
		this.exponent = exponent;
	}

	public final SignNode<SignOfNumber> getSign() {
		return this.sign;
	}

	public final boolean isNegative() {

		final SignNode<SignOfNumber> sign = getSign();

		return sign != null && sign.getType().isNegative();
	}

	public final SignNode<Radix> getRadixPrefix() {
		return this.radixPrefix;
	}

	public final Radix getRadix() {

		final SignNode<Radix> radixPrefix = getRadixPrefix();

		return radixPrefix != null ? radixPrefix.getType() : DECIMAL_RADIX;
	}

	public final DigitsNode getInteger() {
		return this.integer;
	}

	public final FractionalPartNode getFractional() {
		return this.fractional;
	}

	public final ExponentNode getExponent() {
		return this.exponent;
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
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final TypeNode toType() {
		return null;
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
		if (this.sign != null) {
			this.sign.printContent(out);
		}
		if (this.integer != null) {
			this.integer.printContent(out);
		}
		if (this.fractional != null) {
			this.fractional.printContent(out);
		}
		if (this.exponent != null) {
			this.exponent.printContent(out);
		}
	}

}
