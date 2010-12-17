/*
    Parser
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class OperandParser implements Parser<ExpressionNode> {

	private final Grammar grammar;

	public OperandParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public ExpressionNode parse(ParserContext context) {

		final ExpressionNode expression =
			context.parse(this.grammar.simpleExpression());

		if (expression == null) {
			return null;
		}
		if (!context.isEOF()) {

			final BinaryNode binaryExpression =
				context.parse(this.grammar.binaryExpression(expression));

			if (binaryExpression != null) {
				return binaryExpression;
			}
		}

		return expression;
	}

}
