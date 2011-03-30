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


public class StringLiteralParser implements Parser<StringNode> {

	public static final StringLiteralParser STRING_LITERAL =
		new StringLiteralParser();

	private static final MultiLineStringLiteralParser MULTI_LINE =
		new MultiLineStringLiteralParser();
	private static final EscapeSequenceParser ESCAPE_SEQUENCE =
		new EscapeSequenceParser();
	private static final UnicodeSequenceParser UNICODE_SEQUENCE =
		new UnicodeSequenceParser();

	private StringLiteralParser() {
	}

	@Override
	public StringNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();
		final Quote quote;
		final int closingQuote;

		switch (context.next()) {
		case '\'':
			quote = StringNode.SINGLE_QUOTE;
			closingQuote = '\'';
			break;
		case '\"':
			quote = StringNode.DOUBLE_QUOTE;
			closingQuote = '"';
			break;
		case '\\':
			return context.parse(MULTI_LINE);
		default:
			return null;
		}

		context.skip();

		final SignNode<Quote> opening =
			new SignNode<Quote>(start, context.current(), quote);
		final SignNode<Quote> closing;
		final StringBuilder text = new StringBuilder();

		for (;;) {

			final int c = context.next();

			if (c == '\\') {
				context.acceptAll();

				final int symbol = context.parse(ESCAPE_SEQUENCE);

				if (symbol >= 0) {
					text.append((char) symbol);
				} else {
					text.append('\\');
				}

				continue;
			}
			if (c == closingQuote) {

				final FixedPosition closingStart = context.current().fix();

				context.acceptAll();
				closing = new SignNode<Quote>(
						closingStart,
						context.current(),
						opening.getType().getClosing());
				break;
			}
			if (c == '\n' || c < 0) {
				context.getLogger().unterminatedStringLiteral(opening);
				context.acceptAll();
				return new StringNode(
						opening,
						text.toString(),
						context.current());
			}

			text.append((char) c);
		}

		return context.acceptComments(
				false,
				new StringNode(opening, text.toString(), closing));
	}

	private static final class EscapeSequenceParser implements Parser<Integer> {

		@Override
		public Integer parse(ParserContext context) {

			int c = context.next();

			switch (c) {
			case '\'':
			case '"':
			case '\\':
				context.acceptAll();
				return c;
			case 'n':
				context.acceptAll();
				return Integer.valueOf('\n');
			case 'r':
				context.acceptAll();
				return Integer.valueOf('\r');
			case 't':
				context.acceptAll();
				return Integer.valueOf('\t');
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				return context.parse(UNICODE_SEQUENCE);
			}

			context.getLogger().unrecognizedEscapeSequence(
					context.current(),
					"\\" + c);

			return -1;
		}

	}

	private static final class UnicodeSequenceParser
			implements Parser<Integer> {

		@Override
		public Integer parse(ParserContext context) {

			int symbol = -1;
			boolean first = true;

			for (;;) {

				final int c = context.next();
				final int digit;

				if (c == '\\') {
					context.acceptAll();
					return symbol;
				} else if ('0' <= c && c <= '9') {
					digit = c - '0';
				} else if ('a' <= c && c <= 'f') {
					digit = c - 'a' + 10;
				} else if ('A' <= c && c <= 'F') {
					digit = c - 'A' + 10;
				} else {
					context.getLogger().unterminatedUnicodeEscapeSequence(
							context.current());
					return -1;
				}

				if (first) {
					symbol = digit;
					first = false;
				} else {
					symbol <<= 4;
					symbol |= digit;
				}
			}
		}

	}

}
