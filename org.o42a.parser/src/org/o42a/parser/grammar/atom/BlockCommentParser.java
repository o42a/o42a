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

import static org.o42a.parser.Grammar.whitespace;
import static org.o42a.parser.grammar.atom.CommentBoundParser.BLOCK_BOUND;

import org.o42a.ast.atom.CommentBound;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class BlockCommentParser implements Parser<CommentNode> {

	private final SignNode<CommentBound> opening;

	BlockCommentParser(SignNode<CommentBound> opening) {
		this.opening = opening;
	}

	@Override
	public CommentNode parse(ParserContext context) {

		final SignNode<CommentBound> opening = opening(context);

		if (opening == null) {
			return null;
		}
		if (findNewLine(context) >= 0) {
			return null;
		}
		if (context.isEOF()) {
			return new CommentNode(
					opening,
					"",
					context.firstUnaccepted().fix());
		}

		final StringBuilder text = new StringBuilder();
		int next = context.next();

		for (;;) {
			if (next == '~' && context.isLineStart()) {

				final SignNode<CommentBound> closing =
						context.push(BLOCK_BOUND);

				if (closing != null) {

					final int whitespaces = findNewLine(context);

					if (whitespaces < 0) {
						context.parse(whitespace(true));
						return new CommentNode(
								opening,
								text.toString(),
								closing);
					}

					appendTildes(text, closing);
					appendChars(text, whitespaces, ' ');
					next = context.pendingOrNext();

					continue;
				}
			} else if (next < 0) {
				context.acceptAll();
				return new CommentNode(
						opening,
						text.toString(),
						context.firstUnaccepted().fix());
			}

			text.appendCodePoint(next);
			next = context.next();
		}
	}

	private SignNode<CommentBound> opening(ParserContext context) {
		if (this.opening != null) {
			return this.opening;
		}
		return context.push(BLOCK_BOUND);
	}

	private int findNewLine(ParserContext context) {

		final int start = context.current().column();

		context.push(whitespace(false));

		final int whitespaces = context.current().column() - start;

		if (context.isEOF()) {
			context.acceptAll();
			return -1;
		}
		if (context.pendingOrNext() == '\n') {
			context.acceptAll();
			return -1;
		}

		return whitespaces;
	}

	private void appendTildes(
			StringBuilder text,
			SignNode<CommentBound> closing) {

		int tildes =
				closing.getEnd().getColumn()
				- closing.getStart().getColumn();

		appendChars(text, tildes, '~');
	}

	private void appendChars(StringBuilder text, int numChars, char c) {
		text.ensureCapacity(text.length() + numChars);

		int charsLeft = numChars;

		do {
			text.append(c);
			--charsLeft;
		} while (charsLeft > 0);
	}

}
