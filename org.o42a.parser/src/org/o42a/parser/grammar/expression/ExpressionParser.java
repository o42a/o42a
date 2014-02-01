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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.binary;
import static org.o42a.parser.Grammar.simpleExpression;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public final class ExpressionParser implements Parser<ExpressionNode> {

	public static final ExpressionParser EXPRESSION = new ExpressionParser();

	private final ExpressionNode base;

	private ExpressionParser() {
		this.base = null;
	}

	public ExpressionParser(ExpressionNode base) {
		this.base = base;
	}

	@Override
	public ExpressionNode parse(ParserContext context) {

		final ExpressionNode left = parseLeft(context);

		if (left == null) {
			return null;
		}
		if (!context.isEOF()) {

			final BinaryNode binary = context.parse(binary(left));

			if (binary != null) {
				return binary;
			}
		}

		return left != this.base ? left : null;
	}

	private ExpressionNode parseLeft(ParserContext context) {
		if (this.base == null) {
			return context.parse(simpleExpression());
		}

		final ExpressionNode simple =
				context.parse(simpleExpression(this.base));

		if (simple != null) {
			return simple;
		}

		return this.base;
	}

}
