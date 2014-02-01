/*
    Parser
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
package org.o42a.parser.grammar.clause;

import static org.o42a.parser.Grammar.ref;
import static org.o42a.util.string.Characters.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class BinaryClauseIdParser implements Parser<BinaryNode> {

	private static final OperatorParser OPERATOR = new OperatorParser();

	private final RefNode leftOperand;

	BinaryClauseIdParser(RefNode leftOperand) {
		this.leftOperand = leftOperand;
	}

	@Override
	public BinaryNode parse(ParserContext context) {

		final SignNode<BinaryOperator> sign = context.parse(OPERATOR);

		if (sign == null) {
			return null;
		}

		final BinaryOperator operator = sign.getType();
		final RefNode rightOperand = context.parse(ref());

		if (rightOperand == null) {
			context.getLogger().missingRightOperand(
					context.current(),
					operator.getSign());
		}

		return new BinaryNode(this.leftOperand, sign, rightOperand);
	}

	private static final class OperatorParser
			implements Parser<SignNode<BinaryOperator>> {

		@Override
		public SignNode<BinaryOperator> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final BinaryOperator operator;

			switch (context.next()) {
			case '-':
			case MINUS_SIGN:
				operator = BinaryOperator.SUBTRACT;
				context.acceptAll();
				break;
			case '*':
			case MULTIPLICATION_SIGN:
			case DOT_OPERATOR:
				operator = BinaryOperator.MULTIPLY;
				context.acceptAll();
				break;
			case '/':
			case DIVISION_SIGN:
			case DIVISION_SLASH:
				operator = BinaryOperator.DIVIDE;
				context.acceptAll();
				break;
			case '+':
				operator = BinaryOperator.ADD;
				context.acceptAll();
				break;
			case '<':
				if (context.next() != '=') {
					return null;
				}
				if (context.next() != '>') {
					return null;
				}
				operator = BinaryOperator.COMPARE;
				context.acceptAll();
				break;
			case '=':
				if (context.next() != '=') {
					return null;
				}
				operator = BinaryOperator.EQUAL;
				context.acceptAll();
				break;
			case '~':
				operator = BinaryOperator.SUFFIX;
				context.acceptAll();
				break;
			default:
				return null;
			}

			final SignNode<BinaryOperator> result = new SignNode<>(
					start,
					context.current().fix(),
					operator);

			return context.acceptComments(false, result);
		}

	}

}
