/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.expression;
import static org.o42a.util.string.Characters.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.AssignableNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class AssignmentParser implements Parser<AssignmentNode> {

	private final AssignableNode destination;

	public AssignmentParser(AssignableNode destination) {
		this.destination = destination;
	}

	@Override
	public AssignmentNode parse(ParserContext context) {

		final SignNode<AssignmentOperator> operator = parseOperator(context);

		if (operator == null) {
			return null;
		}

		final ExpressionNode value = context.parse(expression());

		if (value == null) {
			context.getLogger().missingValue(operator);
		}

		return new AssignmentNode(this.destination, operator, value);
	}

	private SignNode<AssignmentOperator> parseOperator(ParserContext context) {

		final AssignmentOperator operator;
		final SourcePosition operatorStart = context.current().fix();

		switch (context.next()) {
		case '<':
			switch (context.next()) {
			case '-':
				operator = AssignmentOperator.BIND;
				context.acceptAll();
				break;
			case '<':
				operator = AssignmentOperator.ASSIGN;
				context.acceptAll();
				break;
			default:
				return null;
			}
			break;
		case '+':
			if (!acceptCombined(context)) {
				return null;
			}
			operator = AssignmentOperator.ADD_AND_ASSIGN;
			break;
		case '-':
		case MINUS_SIGN:
			if (!acceptCombined(context)) {
				return null;
			}
			operator = AssignmentOperator.SUBTRACT_AND_ASSIGN;
			break;
		case '*':
		case MULTIPLICATION_SIGN:
		case DOT_OPERATOR:
			if (!acceptCombined(context)) {
				return null;
			}
			operator = AssignmentOperator.MULTIPLY_AND_ASSIGN;
			break;
		case '/':
		case DIVISION_SIGN:
		case DIVISION_SLASH:
			if (!acceptCombined(context)) {
				return null;
			}
			operator = AssignmentOperator.DIVIDE_AND_ASSIGN;
			break;
		default:
			return null;
		}

		return context.acceptComments(
				false,
				new SignNode<>(
						operatorStart,
						context.current().fix(),
						operator));
	}

	private boolean acceptCombined(ParserContext context) {
		if (context.next() != '<' || context.next() != '<') {
			return false;
		}
		context.acceptAll();
		return true;
	}

}
