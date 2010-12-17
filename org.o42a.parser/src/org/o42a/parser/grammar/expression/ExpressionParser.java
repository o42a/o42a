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

import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public abstract class ExpressionParser implements Parser<ExpressionNode> {

	public static final Parser<ExpressionNode> DECLARATIVE_EXPRESSION =
		new Declarative();
	public static final Parser<ExpressionNode> IMPERATIVE_EXPRESSION =
		new Imperative();

	private ExpressionParser(Grammar grammar) {
	}

	@Override
	public ExpressionNode parse(ParserContext context) {
		return context.parse(DECLARATIVE.operand());
	}

	private static final class Declarative implements Parser<ExpressionNode> {

		@Override
		public ExpressionNode parse(ParserContext context) {
			return context.parse(DECLARATIVE.operand());
		}

	}

	private static final class Imperative implements Parser<ExpressionNode> {

		@Override
		public ExpressionNode parse(ParserContext context) {
			return context.parse(IMPERATIVE.operand());
		}

	}

}
