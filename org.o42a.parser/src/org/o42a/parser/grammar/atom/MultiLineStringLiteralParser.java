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
package org.o42a.parser.grammar.atom;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.atom.StringNode.Quote;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class MultiLineStringLiteralParser implements Parser<StringNode> {

	@Override
	public StringNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();

		if (context.next() != '\\') {
			return null;
		}

		final Quote quote;
		final int closingQuote;

		switch (context.next()) {
		case '\'':
			quote = StringNode.MULTILINE_SINGLE_QUOTE;
			closingQuote = '\'';
			break;
		case '\"':
			quote = StringNode.MULTILINE_DOUBLE_QUOTE;
			closingQuote = '"';
			break;
		default:
			return null;
		}

		context.skip();

		final SignNode<Quote> opening =
			new SignNode<Quote>(start, context.current(), quote);
		final SignNode<Quote> closing;
		boolean multiline = false;
		boolean nl = false;
		final StringBuilder text = new StringBuilder();
		final StringBuilder line = new StringBuilder();
		int c = context.next();

		for (;;) {
			if (c == '\n') {

				final String trimmed = trimEnding(line);

				if (!multiline) {
					if (!trimmed.isEmpty()) {
						text.append(trimmed);
						nl = true;
					}
					multiline = true;
				} else {
					if (nl) {
						text.append('\n');
					} else {
						nl = true;
					}
					text.append(trimmed);
				}
				line.setLength(0);
				c = context.next();
				continue;
			}
			if (c == closingQuote) {

				final FixedPosition closingStart = context.current().fix();
				final int next = context.next();

				if (next != '\\') {
					line.append((char) c);
					c = next;
					continue;
				}

				context.acceptAll();
				closing = new SignNode<Quote>(
						closingStart,
						context.current(),
						opening.getType().getClosing());
				break;
			}
			if (c < 0) {
				context.getLogger().unterminatedStringLiteral(opening);
				context.acceptAll();
				return new StringNode(
						opening,
						text.toString(),
						context.current());
			}

			line.append((char) c);
			c = context.next();
		}

		if (!multiline) {
			text.append(line);
		} else {

			final String trimmed = trimEnding(line);

			if (!trimmed.isEmpty()) {
				if (nl) {
					text.append('\n');
				}
				text.append(line);
			}
		}

		final StringNode result = new StringNode(
				opening,
				text.toString(),
				closing);

		return context.acceptComments(false, result);
	}

	private static String trimEnding(StringBuilder line) {

		int len = line.length();

		while (len > 0) {

			final int i = len - 1;
			final char c = line.charAt(i);

			if (!Character.isWhitespace(c)) {
				break;
			}

			len = i;
		}

		return line.substring(0, len);
	}

}
