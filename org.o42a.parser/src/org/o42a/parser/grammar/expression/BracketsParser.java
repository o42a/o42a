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

import static org.o42a.ast.atom.BracketSign.CLOSING_BRACKET;
import static org.o42a.ast.atom.BracketSign.OPENING_BRACKET;
import static org.o42a.ast.statement.AssignmentOperator.ASSIGN;
import static org.o42a.parser.Grammar.expression;

import java.util.ArrayList;

import org.o42a.ast.atom.*;
import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.parser.Expectations;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public final class BracketsParser implements Parser<BracketsNode> {

	public static final BracketsParser BRACKETS = new BracketsParser();

	private BracketsParser() {
	}

	@Override
	public BracketsNode parse(ParserContext context) {

		final SignNode<BracketSign> opening = parseOpening(context);

		if (opening == null) {
			return null;
		}

		final ArrayList<ArgumentNode> arguments = new ArrayList<>();
		SignNode<BracketSign> closing = null;
		SignNode<CommaSign> separator = null;
		SeparatorNodes prevSeparators = null;

		final Expectations expectations =
				context.expectNothing().expect(',').expect(']');

		for (;;) {

			final ArgumentNode argument =
					expectations.parse(new ArgumentParser(separator));

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
			final int c = context.pendingOrNext();

			if (c == ']') {

				final SourcePosition closingStart = context.current().fix();

				context.acceptAll();

				closing = new SignNode<>(
						closingStart,
						context.current().fix(),
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

			final SourcePosition separatorStart = context.current().fix();

			context.acceptAll();
			separator = new SignNode<>(
					separatorStart,
					context.current().fix(),
					CommaSign.COMMA);
			separator.addComments(prevSeparators);
			prevSeparators = null;
			separator.addComments(separators);
			context.acceptComments(true, separator);
		}

		return context.acceptComments(
				false,
				new BracketsNode(
						opening,
						arguments.toArray(new ArgumentNode[arguments.size()]),
						closing));
	}

	private static SignNode<BracketSign> parseOpening(ParserContext context) {
		if (context.next() != '[') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		return context.acceptComments(
				true,
				new SignNode<>(
						start,
						context.current().fix(),
						OPENING_BRACKET));
	}

	private static SignNode<AssignmentOperator> parseInit(
			ParserContext context) {
		if (context.pendingOrNext() != '=') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.skip();

		return context.skipComments(
				true,
				new SignNode<>(
						start,
						context.current().fix(),
						ASSIGN));
	}

	private static final class ArgumentParser implements Parser<ArgumentNode> {

		private final SignNode<CommaSign> separator;

		ArgumentParser(SignNode<CommaSign> separator) {
			this.separator = separator;
		}

		@Override
		public ArgumentNode parse(ParserContext context) {

			SignNode<AssignmentOperator> init = null;
			SourcePosition firstUnexpected = null;

			for (;;) {

				final SourcePosition start = context.current().fix();

				if (init == null) {
					init = parseInit(context);
					if (init != null) {
						context.logUnexpected(firstUnexpected, start);
						context.acceptUnexpected();
						firstUnexpected = null;
					}
				}

				final ExpressionNode value = context.parse(expression());

				if (value != null) {
					context.logUnexpected(firstUnexpected, start);
					context.acceptUnexpected();
					return new ArgumentNode(this.separator, init, value);
				}

				// Unexpected input before the argument.
				// Possibly multiple lines.
				if (context.isEOF()) {
					if (firstUnexpected != null) {
						context.logUnexpected(firstUnexpected, start);
					} else {
						context.getLogger().eof(start);
					}
					return null;
				}
				if (!context.unexpected()) {
					context.logUnexpected(firstUnexpected, start);
					return null;
				}
				if (firstUnexpected == null) {
					firstUnexpected = start;
				}
				context.acceptAll();
				if (context.acceptComments(true) != null) {
					context.logUnexpected(firstUnexpected, start);
					firstUnexpected = null;
				}
			}
		}

	}

}
