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

import static java.lang.Character.isWhitespace;
import static org.o42a.parser.grammar.atom.StringBoundParser.quotedLine;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringBound;
import org.o42a.ast.atom.StringNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class TextBlockParser implements Parser<StringNode> {

	private final SignNode<StringBound> openingBound;

	TextBlockParser(SignNode<StringBound> openingBound) {
		this.openingBound = openingBound;
	}

	@Override
	public StringNode parse(ParserContext context) {

		final boolean doubleQuoted =
				this.openingBound.getType().isDoubleQuoted();
		final int closingQuote = doubleQuoted ? '"' : '\'';
		boolean nl = false;
		final StringBuilder text = new StringBuilder();
		final StringBuilder line = new StringBuilder();

		for (;;) {

			final int c = context.next();

			if (c == '\n') {
				if (nl) {
					text.append('\n');
					nl = false;
				} else {
					nl = true;
				}
				text.append(trimEnding(line));
				line.setLength(0);
				continue;
			}
			if (c == closingQuote && context.isLineStart()) {

				final SignNode<StringBound> closingBound =
						context.push(quotedLine(doubleQuoted));

				if (closingBound  != null) {

					final StringNode string = new StringNode(
							this.openingBound,
							text.toString(),
							closingBound);

					context.acceptAll();

					return context.acceptComments(false, string);
				}
				line.appendCodePoint(c);
				continue;
			}
			if (c < 0) {
				context.getLogger()
				.unterminatedStringLiteral(this.openingBound);
				context.acceptAll();
				if (nl) {
					text.append('\n').append(trimEnding(line));
				}
				return new StringNode(
						this.openingBound,
						text.toString(),
						context.current().fix());
			}

			line.appendCodePoint(c);
		}
	}

	private static String trimEnding(StringBuilder line) {

		int len = line.length();

		while (len > 0) {

			final int i = len - 1;
			final char c = line.charAt(i);

			if (!isWhitespace(c)) {
				break;
			}

			len = i;
		}

		return line.substring(0, len);
	}

}
