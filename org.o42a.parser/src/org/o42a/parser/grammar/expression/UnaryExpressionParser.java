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

import static org.o42a.parser.Grammar.simpleExpression;
import static org.o42a.util.string.Characters.MINUS;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class UnaryExpressionParser implements Parser<UnaryNode> {

	public static final UnaryExpressionParser UNARY_EXPRESSION =
			new UnaryExpressionParser();

	private UnaryExpressionParser() {
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
		case MINUS:
			switch (context.next()) {
			case '-':
				operator = UnaryOperator.NOT;
				context.acceptAll();
				break;
			default:
				operator = UnaryOperator.MINUS;
				context.acceptButLast();
			}
			break;
		default:
			return null;
		}

		final SignNode<UnaryOperator> sign = new SignNode<UnaryOperator>(
				start,
				context.firstUnaccepted().fix(),
				operator);

		context.acceptComments(false, sign);

		final ExpressionNode parameter = context.parse(simpleExpression());

		if (parameter == null) {
			context.getLogger().missingOperand(
					context.current(),
					operator.getSign());
		}

		return new UnaryNode(sign, parameter);
	}

}
