/*
    Parser
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static java.lang.Character.isWhitespace;
import static org.o42a.ast.atom.StringBound.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringBound;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public final class StringBoundParser implements Parser<SignNode<StringBound>> {

	public static final StringBoundParser STRING_BOUND =
			new StringBoundParser();

	private static final QuotedLineParser DOUBLE_QUOTED =
			new QuotedLineParser('"');
	private static final QuotedLineParser SINGLE_QUOTED =
			new QuotedLineParser('\'');

	static Parser<SignNode<StringBound>> quotedLine(	boolean doubleQuoted) {
		return doubleQuoted ? DOUBLE_QUOTED : SINGLE_QUOTED;
	}

	private StringBoundParser() {
	}

	@Override
	public SignNode<StringBound> parse(ParserContext context) {

		final SourcePosition start = context.current().fix();
		final int first = context.next();
		final boolean doubleQuoted;

		switch (first) {
		case '"':
			doubleQuoted = true;
			break;
		case '\'':
			doubleQuoted = false;
			break;
		default:
			return null;
		}

		if (context.isLineStart()) {

			final SignNode<StringBound> line =
					context.push(quotedLine(doubleQuoted));

			if (line != null) {
				context.acceptAll();
				return line;
			}
		}

		context.acceptAll();

		return new SignNode<>(
				start,
				context.firstUnaccepted().fix(),
				doubleQuoted ? DOUBLE_QUOTE : SINGLE_QUOTE);
	}

	private static final class QuotedLineParser
			implements Parser<SignNode<StringBound>> {

		private final int quote;

		QuotedLineParser(int quote) {
			this.quote = quote;
		}

		@Override
		public SignNode<StringBound> parse(ParserContext context) {
			if (!context.isLineStart()) {
				return null;
			}
			if (context.next() != this.quote) {
				return null;
			}

			final SourcePosition start = context.current().fix();
			int count = 1;
			int next;

			for (;;) {
				next = context.next();
				if (next != this.quote) {
					break;
				}
				++count;
			}

			if (count < 3) {
				return null;
			}
			if (!skipTerminalBlanks(context, next)) {
				return null;
			}

			context.acceptAll();

			return new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					this.quote == '"'
					? DOUBLE_QUOTED_LINE : SINGLE_QUOTED_LINE);
		}


		private boolean skipTerminalBlanks(ParserContext context, int start) {

			int c = start;

			for (;;) {
				if (c < 0 || c == '\n') {
					return true;
				}
				if (!isWhitespace(c)) {
					return false;
				}
				c = context.next();
			}
		}

	}

}
