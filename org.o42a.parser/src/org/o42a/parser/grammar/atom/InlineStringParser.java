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
package org.o42a.parser.grammar.atom;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringBound;
import org.o42a.ast.atom.StringNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class InlineStringParser implements Parser<StringNode> {

	private static final EscapeSequenceParser ESCAPE_SEQUENCE =
			new EscapeSequenceParser();
	private static final UnicodeSequenceParser UNICODE_SEQUENCE =
			new UnicodeSequenceParser();

	private final SignNode<StringBound> openingBound;

	InlineStringParser(SignNode<StringBound> openingBound) {
		this.openingBound = openingBound;
	}

	@Override
	public StringNode parse(ParserContext context) {

		final int closingQuote =
				this.openingBound.getType().isDoubleQuoted() ? '"' : '\'';
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

				final SourcePosition closingStart = context.current().fix();

				context.acceptAll();

				final SignNode<StringBound> closingBound = new SignNode<>(
						closingStart,
						context.current().fix(),
						this.openingBound.getType());
				final StringNode string = new StringNode(
						this.openingBound,
						text.toString(),
						closingBound);

				return context.acceptComments(false, string);
			}
			if (c == '\n' || c < 0) {
				context.getLogger()
				.unterminatedStringLiteral(this.openingBound);
				context.acceptAll();
				return new StringNode(
						this.openingBound,
						text.toString(),
						context.current().fix());
			}

			text.appendCodePoint(c);
		}
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
