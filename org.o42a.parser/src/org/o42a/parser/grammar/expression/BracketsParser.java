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

import static org.o42a.ast.expression.BracketsNode.Bracket.CLOSING_BRACKET;
import static org.o42a.ast.expression.BracketsNode.Bracket.OPENING_BRACKET;
import static org.o42a.parser.grammar.field.InterfaceParser.INTERFACE;

import java.util.ArrayList;

import org.o42a.ast.EmptyNode;
import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.expression.ArgumentNode.Separator;
import org.o42a.ast.expression.BracketsNode.Bracket;
import org.o42a.ast.field.InterfaceNode;
import org.o42a.parser.*;


public class BracketsParser implements Parser<BracketsNode> {

	private final Parser<ExpressionNode> elementParser;

	public BracketsParser(Grammar grammar) {
		this.elementParser = grammar.expression();
	}

	@Override
	public BracketsNode parse(ParserContext context) {
		if (context.next() != '[') {
			return null;
		}

		final SignNode<Bracket> opening = opening(context);
		final InterfaceNode iface = context.parse(INTERFACE);
		final ArrayList<ArgumentNode> arguments = new ArrayList<ArgumentNode>();
		SignNode<Bracket> closing = null;
		SignNode<Separator> separator = null;
		SeparatorNodes prevSeparators = null;

		final Expectations expectations =
				context.expectNothing().expect(',').expect(']');

		for (;;) {

			final ArgumentNode argument = expectations.parse(
					new ArgumentParser(separator, this.elementParser));

			if (argument != null) {
				argument.addComments(prevSeparators);
				prevSeparators = null;
				arguments.add(argument);
			} else if (separator != null) {

				final ArgumentNode emptyArg =
						new ArgumentNode(separator.getEnd());

				arguments.add(emptyArg);
			}

			final SeparatorNodes separators = context.acceptComments(true);
			final int c = context.next();

			if (c == ']') {

				final FixedPosition closingStart = context.current().fix();

				context.acceptAll();

				closing = new SignNode<Bracket>(
						closingStart,
						context.current(),
						CLOSING_BRACKET);
				closing.addComments(prevSeparators);
				prevSeparators = null;
				closing.addComments(separators);
				break;
			}
			if (c != ',') {
				if (separators == null) {
					context.getLogger().notClosed(opening, "[");
					context.acceptButLast();
					break;
				}
				prevSeparators = separators;
				continue;
			}
			if (argument == null && separator == null) {

				final ArgumentNode emptyArg =
						new ArgumentNode(opening.getEnd());

				emptyArg.addComments(separators);
				arguments.add(emptyArg);
			}

			final FixedPosition separatorStart = context.current().fix();

			context.acceptAll();
			separator = new SignNode<Separator>(
					separatorStart,
					context.current(),
					Separator.COMMA);
			separator.addComments(prevSeparators);
			prevSeparators = null;
			separator.addComments(separators);
			context.acceptComments(true, separator);
		}

		return context.acceptComments(
				false,
				new BracketsNode(
						opening,
						iface,
						arguments.toArray(new ArgumentNode[arguments.size()]),
						closing));
	}

	private SignNode<Bracket> opening(ParserContext context) {

		final FixedPosition start = context.current().fix();

		context.acceptAll();

		return context.acceptComments(
				true,
				new SignNode<Bracket>(
						start,
						context.current(),
						OPENING_BRACKET));
	}

	private static void logUnexpected(
			ParserContext context,
			FixedPosition firstUnexpected,
			FixedPosition current) {
		if (firstUnexpected == null) {
			return;
		}
		context.getLogger().syntaxError(
				new EmptyNode(firstUnexpected, current));
	}

	private static final class ArgumentParser implements Parser<ArgumentNode> {

		private final SignNode<Separator> separator;
		private final Parser<ExpressionNode> elementParser;

		ArgumentParser(
				SignNode<Separator> separator,
				Parser<ExpressionNode> elementParser) {
			this.separator = separator;
			this.elementParser = elementParser;
		}

		@Override
		public ArgumentNode parse(ParserContext context) {

			FixedPosition firstUnexpected = null;

			for (;;) {

				final FixedPosition start = context.current().fix();
				final ExpressionNode value = context.parse(this.elementParser);

				if (value != null) {
					logUnexpected(context, firstUnexpected, start);
					return new ArgumentNode(this.separator, value);
				}
				if (context.isEOF()) {
					if (firstUnexpected != null) {
						logUnexpected(context, firstUnexpected, start);
					} else {
						context.getLogger().eof(start);
					}
					return null;
				}
				if (context.asExpected()) {
					logUnexpected(context, firstUnexpected, start);
					return null;
				}
				if (firstUnexpected == null) {
					if (context.skipComments(true) != null) {
						return null;
					}
					firstUnexpected = start;
				}
				context.acceptAll();
				if (context.acceptComments(true) != null) {
					logUnexpected(context, firstUnexpected, start);
					firstUnexpected = null;
				}
			}
		}

	}

}
