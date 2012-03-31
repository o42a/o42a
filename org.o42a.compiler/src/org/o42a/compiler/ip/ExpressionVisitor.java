/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorSpecVisitor.parseAncestor;
import static org.o42a.compiler.ip.Interpreter.integer;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.*;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.value.ValueType.STRING;

import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.ValueTypeNode;
import org.o42a.compiler.ip.operator.LogicalExpression;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;


public final class ExpressionVisitor
		extends AbstractExpressionVisitor<Ref, Distributor> {

	private final Interpreter ip;
	private final Referral referral;

	protected ExpressionVisitor(Interpreter ip, Referral referral) {
		this.ip = ip;
		this.referral = referral;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public Ref visitDecimal(DecimalNode decimal, Distributor p) {
		return integer(decimal, p);
	}

	@Override
	public Ref visitText(TextNode textNode, Distributor p) {

		final String text = textNode.getText();

		if (textNode.isDoubleQuote()) {
			return STRING.constantRef(location(p, textNode), p, text);
		}

		return super.visitText(textNode, p);
	}

	@Override
	public Ref visitUnary(UnaryNode expression, Distributor p) {
		switch (expression.getOperator()) {
		case PLUS:
		case MINUS:

			final Phrase phrase = unary(ip(), expression, p);

			if (phrase == null) {
				return null;
			}

			return phrase.toRef();
		case IS_TRUE:
		case NOT:
		case KNOWN:
		case UNKNOWN:
			return new LogicalExpression(
					ip(),
					p.getContext(),
					expression,
					p).toRef();
		}

		return super.visitUnary(expression, p);
	}

	@Override
	public Ref visitBinary(BinaryNode expression, Distributor p) {
		return binary(ip(), expression, p);
	}

	@Override
	public Ref visitBrackets(BracketsNode brackets, Distributor p) {
		return new ArrayConstructor(
				this.ip,
				p.getContext(),
				brackets,
				p).toRef();
	}

	@Override
	public Ref visitParentheses(ParenthesesNode parentheses, Distributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	public Ref visitAscendants(AscendantsNode ascendants, Distributor p) {
		if (ascendants.getAscendants().length <= 1) {

			final AncestorTypeRef ancestor =
					parseAncestor(ip(), ascendants, p);

			if (ancestor.isImplied()) {
				return super.visitAscendants(ascendants, p);
			}

			return ancestor.getAncestor().getRef();
		}

		return ascendants(ip(), ascendants, p).toRef();
	}

	@Override
	public Ref visitValueType(ValueTypeNode valueType, Distributor p) {

		final Phrase phrase = ascendants(ip(), valueType, p);

		if (phrase == null) {
			return super.visitValueType(valueType, p);
		}

		return phrase.toRef();
	}

	@Override
	public Ref visitPhrase(PhraseNode phrase, Distributor p) {
		return phrase(ip(), phrase, p).toRef();
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {
		return ref.accept(this.referral.refVisitor(ip()), p);
	}

	@Override
	protected Ref visitExpression(ExpressionNode expression, Distributor p) {
		p.getLogger().invalidExpression(expression);
		return errorRef(location(p, expression), p);
	}

}
