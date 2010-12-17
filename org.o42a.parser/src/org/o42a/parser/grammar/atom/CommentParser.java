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
package org.o42a.parser.grammar.atom;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.CommentNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class CommentParser implements Parser<CommentNode> {

	public static final CommentParser COMMENT = new CommentParser();

	private CommentParser() {
	}

	@Override
	public CommentNode parse(ParserContext context) {
		context.parse(WhitespaceParser.WHITESPACE);
		if (context.next() != '/') {
			return null;// not a first comment symbol
		}

		final FixedPosition start = context.current().fix();
		final boolean multiline;

		switch (context.next()) {
		case '/':
			multiline = false;
			break;
		case '*':
			multiline = true;
			break;
		default:
			return null;
		}

		final StringBuilder text = new StringBuilder();

		for (;;) {

			final int c = context.next();

			if (c < 0) {
				if (multiline) {
					context.getLogger().eof(context.current());
				}
				context.acceptAll();
				break;
			}
			if (multiline) {
				if (c == '/') {

					final int lastIdx = text.length() - 1;

					if (lastIdx >= 0 && text.charAt(lastIdx) == '*') {
						text.setLength(lastIdx);
						context.acceptAll();
						break;
					}
				}
			} else if (c == '\n') {
				context.acceptBut(1);
				break;
			}

			text.append((char) c);
		}

		final CommentNode comment = new CommentNode(
				start,
				context.firstUnaccepted(),
				multiline,
				text.toString());

		if (!context.isEOF()) {
			context.parse(WhitespaceParser.WHITESPACE);
		}
		context.acceptAll();

		return comment;
	}

}
