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
import static org.o42a.parser.grammar.atom.CommentBoundParser.INLINE_BOUND;

import org.o42a.ast.atom.CommentBound;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class InlineCommentParser implements Parser<CommentNode> {

	public static final InlineCommentParser INLINE_COMMENT =
			new InlineCommentParser(null, false);

	private final SignNode<CommentBound> opening;
	private final boolean allowNewLine;

	InlineCommentParser(SignNode<CommentBound> opening, boolean allowNewLine) {
		this.opening = opening;
		this.allowNewLine = allowNewLine;
	}

	@Override
	public CommentNode parse(ParserContext context) {

		final SignNode<CommentBound> opening = opening(context);

		if (opening == null) {
			return null;
		}

		final StringBuilder text = new StringBuilder();

		for (;;) {

			final int c = context.next();

			if (c == '\n') {
				context.acceptButLast();
				break;
			}
			if (c == '~') {

				final SignNode<CommentBound> closing =
						context.push(INLINE_BOUND);

				if (closing != null) {
					context.parse(whitespace(this.allowNewLine));
					context.acceptAll();
					return new CommentNode(
							opening,
							text.toString(),
							closing);
				}
			} else if (c < 0) {
				context.acceptAll();
				break;
			}

			text.appendCodePoint(c);
		}

		final SourcePosition end = context.firstUnaccepted().fix();

		context.parse(whitespace(this.allowNewLine));

		return new CommentNode(
				opening,
				text.toString(),
				end);
	}

	private SignNode<CommentBound> opening(ParserContext context) {
		if (this.opening != null) {
			return this.opening;
		}
		return context.parse(INLINE_BOUND);
	}

}
