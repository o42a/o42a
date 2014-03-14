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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.ReturnNode;
import org.o42a.ast.statement.ReturnOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class ReturnParser implements Parser<ReturnNode> {

	public static final ReturnParser RETURN = new ReturnParser();

	private static final PrefixParser PREFIX = new PrefixParser();

	private ReturnParser() {
	}

	@Override
	public ReturnNode parse(ParserContext context) {

		final SignNode<ReturnOperator> prefix = context.parse(PREFIX);

		if (prefix == null) {
			return null;
		}

		final ExpressionNode value = parseValue(context);

		return new ReturnNode(prefix, value);
	}

	private ExpressionNode parseValue(ParserContext context) {

		final ExpressionNode value = context.parse(expression());

		if (value == null) {
			context.getLogger().missingValue(context.current());
		}

		return value;
	}

	private static final class PrefixParser
			implements Parser<SignNode<ReturnOperator>> {

		@Override
		public SignNode<ReturnOperator> parse(ParserContext context) {
			switch (context.next()) {
			case '=':
				return parseSet(context);
			case '<':
				return parseYield(context);
			}
			return null;
		}

		private SignNode<ReturnOperator> parseSet(
				ParserContext context) {

			final SourcePosition start = context.current().fix();

			switch (context.next()) {
			case '=':
			case '>':
			case '<':
				return null;
			}

			context.acceptButLast();

			return context.acceptComments(
					false,
					new SignNode<>(
							start,
							context.current().fix(),
							ReturnOperator.RETURN_VALUE));
		}

		private SignNode<ReturnOperator> parseYield(
				ParserContext context) {

			final SourcePosition start = context.current().fix();

			if (context.next() != '<') {
				return null;
			}
			if (context.next() == '<') {
				return null;
			}

			context.acceptButLast();

			return context.acceptComments(
					false,
					new SignNode<>(
							start,
							context.current().fix(),
							ReturnOperator.YIELD_VALUE));
		}

	}

}
