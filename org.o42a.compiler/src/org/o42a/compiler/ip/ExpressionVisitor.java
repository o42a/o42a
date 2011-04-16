/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.*;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.value.ValueType.FLOAT;
import static org.o42a.core.value.ValueType.INTEGER;
import static org.o42a.core.value.ValueType.STRING;

import org.o42a.ast.Node;
import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.atom.StringNode.Quote;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.operator.LogicalOperatorRef;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.ref.Ref;


public class ExpressionVisitor
		extends AbstractExpressionVisitor<Ref, Distributor> {

	public static final ExpressionVisitor EXPRESSION_VISITOR =
		new ExpressionVisitor();

	protected ExpressionVisitor() {
	}

	@Override
	public Ref visitDecimal(DecimalNode decimal, Distributor p) {

		final long value;

		try {
			value = Long.parseLong(decimal.getNumber());
		} catch (NumberFormatException e) {
			p.getContext().getLogger().notInteger(decimal, decimal.getNumber());
			return integer(p, 0L, decimal);
		}

		return integer(p, value, decimal);
	}

	@Override
	public Ref visitText(TextNode textNode, Distributor p) {

		final String text = textNode.getText();
		final Quote quote =
			textNode.getLiterals()[0].getOpeningQuotationMark().getType();

		if (quote.isDoubleQuote()) {
			return STRING.definiteRef(location(p, textNode), p, text);
		}

		final long intVal;

		try {
			intVal = Long.parseLong(text);
			return integer(p, intVal, textNode);
		} catch (NumberFormatException e) {
		}

		final double floatVal;

		try {
			floatVal = Double.parseDouble(text);
		} catch (NumberFormatException e) {
			p.getContext().getLogger().notFloat(textNode, text);
			return FLOAT.definiteRef(location(p, textNode), p, 0.0);
		}

		return FLOAT.definiteRef(location(p, textNode), p, floatVal);
	}

	@Override
	public Ref visitUnary(UnaryNode expression, Distributor p) {
		switch (expression.getOperator()) {
		case PLUS:
		case MINUS:

			final Phrase phrase = unary(expression, p);

			if (phrase == null) {
				return null;
			}

			return phrase.toRef();
		case IS_TRUE:
		case NOT:
		case KNOWN:
		case UNKNOWN:
			return new LogicalOperatorRef(p.getContext(), expression, p);
		}

		return super.visitUnary(expression, p);
	}

	@Override
	public Ref visitBinary(BinaryNode expression, Distributor p) {
		return binary(expression, p);
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
		if (ascendants.getAscendants().length == 1) {
			return ascendants.getAscendants()[0].getAscendant().accept(
					REF_VISITOR,
					p).toStatic();
		}
		return ascendants(ascendants, p).toRef();
	}

	@Override
	public Ref visitPhrase(PhraseNode phrase, Distributor p) {
		return phrase(phrase, p).toRef();
	}

	@Override
	public Ref visitArray(ArrayNode array, Distributor p) {
		// TODO Auto-generated method stub
		return super.visitArray(array, p);
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {
		return ref.accept(REF_VISITOR, p);
	}

	@Override
	protected Ref visitExpression(ExpressionNode expression, Distributor p) {
		p.getContext().getLogger().invalidExpression(expression);
		return voidRef(location(p, expression), p);
	}

	private Ref integer(Distributor p, long value, Node node) {

		final Location location = location(p, node);

		return INTEGER.definiteRef(location, p, value);
	}

}
