/*
    Parser
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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.expression;
import static org.o42a.parser.Grammar.scopeRef;
import static org.o42a.util.string.Characters.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
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
			return implicitSuffix(context);
		}

		final BinaryOperator operator = sign.getType();
		ExpressionNode rightOperand = context.parse(expression());

		if (rightOperand == null) {
			context.getLogger().missingRightOperand(
					context.current(),
					operator.getSign());
		} else {

			final BinaryNode updated =
					updatePriority(sign, operator, rightOperand);

			if (updated != null) {
				return updated;
			}
		}

		return new BinaryNode(this.leftOperand, sign, rightOperand);
	}

	private BinaryNode implicitSuffix(ParserContext context) {

		final int next = context.next();

		if (next != '#') {
			return null;
		}

		final BinaryNode suffix = context.parse(
				new ImplicitSuffixParser(this.leftOperand));

		if (suffix == null) {
			return null;
		}

		final BinaryNode updated = updatePriority(
				suffix.getSign(),
				suffix.getOperator(),
				suffix.getRightOperand());

		if (updated != null) {
			return updated;
		}

		return suffix;
	}

	private BinaryNode updatePriority(
			SignNode<BinaryOperator> sign,
			BinaryOperator operator,
			ExpressionNode rightOperand) {

		final BinaryNode right = rightOperand.toBinary();

		if (right == null) {
			// Right operand is not a binary operator.
			// Do not change anything.
			return null;
		}
		if (operator.getPriority() < right.getOperator().getPriority()) {
			// Left operator's priority is less than the right's one.
			// Do not regroup operands.
			return null;
		}

		// Left operator's priority is higher or equal to the right's one.
		// Regroup operands from `a ~ (b ~ c)` to `(a ~ b) ~ c`.
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
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
				break;
			case '*':
			case MULTIPLICATION_SIGN:
			case DOT_OPERATOR:
				operator = BinaryOperator.MULTIPLY;
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
				break;
			case '/':
			case DIVISION_SIGN:
			case DIVISION_SLASH:
				operator = BinaryOperator.DIVIDE;
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
				break;
			case '+':
				operator = BinaryOperator.ADD;
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
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
				case '-':
					return null;
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
			case NOT_EQUAL_TO:
				operator = BinaryOperator.NOT_EQUAL;
				context.acceptAll();
				break;
			case LESS_THAN_OR_EQUAL_TO:
				operator = BinaryOperator.LESS_OR_EQUAL;
				context.acceptAll();
				break;
			case GREATER_THAN_OR_EQUAL_TO:
				operator = BinaryOperator.GREATER_OR_EQUAL;
				context.acceptAll();
				break;
			default:
				return null;
			}

			final SignNode<BinaryOperator> result = new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					operator);

			return context.acceptComments(false, result);
		}

	}

	private static final class ImplicitSuffixParser
			implements Parser<BinaryNode> {

		private final ExpressionNode leftOperand;

		ImplicitSuffixParser(ExpressionNode leftOperand) {
			this.leftOperand = leftOperand;
		}

		@Override
		public BinaryNode parse(ParserContext context) {

			final ScopeRefNode scopeRef = context.checkFor(scopeRef());

			if (scopeRef == null) {
				return null;
			}

			final ScopeType scopeType = scopeRef.getType();

			if (scopeType != ScopeType.MACROS) {
				return null;
			}

			final ExpressionNode rightOperand = context.parse(expression());

			if (rightOperand == null) {
				return null;
			}

			return new BinaryNode(
					this.leftOperand,
					new SignNode<>(
							scopeRef.getStart(),
							scopeRef.getEnd(),
							BinaryOperator.SUFFIX),
					rightOperand);
		}

	}

}
