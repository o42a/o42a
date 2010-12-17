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

import static org.o42a.ast.expression.BracketsNode.Bracket.CLOSING_BRACKET;
import static org.o42a.ast.expression.BracketsNode.Bracket.OPENING_BRACKET;

import java.util.ArrayList;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.expression.ArgumentNode.Separator;
import org.o42a.ast.expression.BracketsNode.Bracket;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class BracketsParser implements Parser<BracketsNode> {

	private final Parser<ExpressionNode> elementParser;

	public BracketsParser(Grammar grammar) {
		this.elementParser = grammar.expression();
	}

	public BracketsParser(Parser<ExpressionNode> elementParser) {
		this.elementParser = elementParser;
	}

	@Override
	public BracketsNode parse(ParserContext context) {
		if (context.next() != '[') {
			return null;
		}

		final FixedPosition start = context.current().fix();

		context.skip();

		final SignNode<Bracket> opening =
			new SignNode<Bracket>(start, context.current(), OPENING_BRACKET);

		context.skipComments(opening);

		int c = context.next();

		if (c == ']') {

			final FixedPosition closingStart = context.current().fix();

			context.acceptAll();

			final SignNode<Bracket> closing = new SignNode<Bracket>(
					closingStart,
					context.current(),
					CLOSING_BRACKET);

			return context.acceptComments(
					new BracketsNode(opening, new ArgumentNode[0], closing));
		}

		final ArrayList<ArgumentNode> arguments = new ArrayList<ArgumentNode>();
		SignNode<Bracket> closing = null;
		SignNode<Separator> separator = null;

		for (;;) {

			final ExpressionNode value = context.parse(this.elementParser);

			arguments.add(new ArgumentNode(separator, value));

			c = context.next();
			if (c == ']') {

				final FixedPosition closingStart =
					context.current().fix();

				context.acceptAll();

				closing = new SignNode<Bracket>(
						closingStart,
						context.current(),
						CLOSING_BRACKET);
				break;
			}
			if (c != ',') {
				context.getLogger().notClosed(opening, "[");
				context.acceptButLast();
				break;
			}

			final FixedPosition separatorStart = context.current().fix();

			context.acceptAll();
			separator = new SignNode<Separator>(
					separatorStart,
					context.current(),
					Separator.COMMA);
			context.acceptComments(separator);
		}

		return context.acceptComments(new BracketsNode(
					opening,
					arguments.toArray(new ArgumentNode[arguments.size()]),
					closing));
	}

}
