/*
    Parser
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
package org.o42a.parser.grammar.expression;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class UnaryExpressionParser implements Parser<UnaryNode> {

	private final Grammar grammar;

	public UnaryExpressionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public UnaryNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();
		final UnaryOperator operator;

		switch (context.next()) {
		case '+':
			operator = UnaryOperator.PLUS;
			context.acceptAll();
			break;
		case '-':
		case Grammar.MINUS:
			operator = UnaryOperator.MINUS;
			context.acceptAll();
			break;
		default:
			return null;
		}

		final SignNode<UnaryOperator> sign = new SignNode<UnaryOperator>(
				start,
				context.firstUnaccepted(),
				operator);

		context.acceptComments(sign);

		final ExpressionNode parameter =
			context.parse(this.grammar.simpleExpression());

		if (parameter == null) {
			context.getLogger().missingOperand(
					context.current(),
					operator.getSign());
		}

		return new UnaryNode(sign, parameter);
	}

}
