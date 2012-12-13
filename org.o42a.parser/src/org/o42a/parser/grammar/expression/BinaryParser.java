/*
    Parser
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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.expression;
import static org.o42a.util.string.Characters.MINUS;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class BinaryParser implements Parser<BinaryNode> {

	private static final OperatorParser OPERATOR = new OperatorParser();

	private final ExpressionNode leftOperand;

	public BinaryParser(ExpressionNode leftOperand) {
		this.leftOperand = leftOperand;
	}

	@Override
	public BinaryNode parse(ParserContext context) {

		final SignNode<BinaryOperator> sign = context.parse(OPERATOR);

		if (sign == null) {
			return null;
		}

		final BinaryOperator operator = sign.getType();
		ExpressionNode rightOperand = context.parse(expression());

		if (rightOperand == null) {
			context.getLogger().missingRightOperand(
					context.current(),
					operator.getSign());
		} else {

			final BinaryNode right = rightOperand.toBinary();

			if (right != null &&
					operator.getPriority()
					> right.getOperator().getPriority()) {

				final BinaryNode expression = new BinaryNode(
						new BinaryNode(
								this.leftOperand,
								sign,
								right.getLeftOperand()),
						right.getSign(),
						right.getRightOperand());

				expression.addComments(right.getComments());

				return expression;
			}
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
			case MINUS:
				operator = BinaryOperator.SUBTRACT;
				context.acceptAll();
				break;
			case '*':
				operator = BinaryOperator.MULTIPLY;
				context.acceptAll();
				break;
			case '/':
				operator = BinaryOperator.DIVIDE;
				context.acceptAll();
				break;
			case '+':
				operator = BinaryOperator.ADD;
				context.acceptAll();
				break;
			case '<':
				switch (context.next()) {
				case '>':
					operator = BinaryOperator.NOT_EQUAL;
					context.acceptAll();
					break;
				case '=':
					if (context.next() == '>') {
						operator = BinaryOperator.COMPARE;
						context.acceptAll();
					} else {
						operator = BinaryOperator.LESS_OR_EQUAL;
						context.acceptButLast();
					}
					break;
				default:
					operator = BinaryOperator.LESS;
					context.acceptButLast();
				}
				break;
			case '>':
				switch (context.next()) {
				case '=':
					operator = BinaryOperator.GREATER_OR_EQUAL;
					context.acceptAll();
					break;
				default:
					operator = BinaryOperator.GREATER;
					context.acceptButLast();
				}
				break;
			case '=':
				switch (context.next()) {
				case '=':
					operator = BinaryOperator.EQUAL;
					context.acceptAll();
					break;
				default:
					return null;
				}
				break;
			case '~':
				operator = BinaryOperator.SUFFIX;
				context.acceptAll();
				break;
			default:
				return null;
			}

			final SignNode<BinaryOperator> result =
					new SignNode<BinaryOperator>(
							start,
							context.current().fix(),
							operator);

			return context.acceptComments(false, result);
		}

	}

}
