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

import static org.o42a.parser.Grammar.macroExpansion;
import static org.o42a.parser.Grammar.simpleExpression;
import static org.o42a.util.string.Characters.MINUS_SIGN;
import static org.o42a.util.string.Characters.NOT_SIGN;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class UnaryParser implements Parser<UnaryNode> {

	public static final UnaryParser UNARY = new UnaryParser();

	private UnaryParser() {
	}

	@Override
	public UnaryNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();
		final UnaryOperator operator;

		switch (context.next()) {
		case '+':
			switch (context.next()) {
			case '+':
				operator = UnaryOperator.IS_TRUE;
				context.acceptAll();
				break;
			default:
				operator = UnaryOperator.PLUS;
				context.acceptButLast();
			}
			break;
		case '-':
		case MINUS_SIGN:
			if (context.next() == '-') {
				operator = UnaryOperator.NOT;
				context.acceptAll();
			} else {
				operator = UnaryOperator.MINUS;
				context.acceptButLast();
			}
			break;
		case '\\':
			if (context.next() == '\\') {
				operator = UnaryOperator.KEEP_VALUE;
				context.acceptAll();
			} else {
				operator = UnaryOperator.VALUE_OF;
				context.acceptButLast();
			}
			break;
		case '#':
			return context.parse(macroExpansion());
		case NOT_SIGN:
			operator = UnaryOperator.NOT;
			context.acceptAll();
			break;
		default:
			return null;
		}

		final SignNode<UnaryOperator> sign = new SignNode<>(
				start,
				context.firstUnaccepted().fix(),
				operator);

		context.acceptComments(false, sign);

		final ExpressionNode operand = context.parse(simpleExpression());

		if (operand == null) {
			context.getLogger().missingOperand(sign, operator.getSign());
		}

		return new UnaryNode(sign, operand);
	}

}
