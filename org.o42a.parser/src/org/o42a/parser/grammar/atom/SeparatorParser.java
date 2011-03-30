/*
    Parser
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.comment;
import static org.o42a.parser.Grammar.whitespace;

import java.util.ArrayList;

import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class SeparatorParser implements Parser<SeparatorNodes> {

	public static final SeparatorParser SEPARATOR =
		new SeparatorParser(false);
	public static final SeparatorParser SEPARATOR_NL =
		new SeparatorParser(true);

	private final boolean allowNewLine;

	private SeparatorParser(boolean allowNewLine) {
		this.allowNewLine = allowNewLine;
	}

	@Override
	public SeparatorNodes parse(ParserContext context) {

		boolean allowNewLine = this.allowNewLine;
		boolean continuation = false;
		Object whitespacePresent = context.parse(whitespace(allowNewLine));
		ArrayList<CommentNode> comments = null;

		for (;;) {

			final CommentNode comment = context.parse(comment(allowNewLine));

			if (comment != null) {
				if (comments == null) {
					comments = new ArrayList<CommentNode>(1);
				}
				comments.add(comment);
			} else {
				context.parse(whitespace(false));
			}

			final int c = context.next();

			if (c == '_') {
				context.acceptAll();
				context.parse(whitespace(true));
				allowNewLine = continuation = true;
				continue;
			}
			if (c == '\n') {

				final SeparatorNodes separators = context.push(SEPARATOR_NL);

				if (separators.lineContinuation()) {
					context.acceptAll();
					if (comments == null) {
						return separators;
					}
					separators.appendCommentsTo(comments);
					return new SeparatorNodes(true, comments);
				}
				break;
			}
			if (comment == null) {
				break;
			}
		}
		if (comments != null) {
			return new SeparatorNodes(continuation, comments);
		}
		if (whitespacePresent == null) {
			return null;
		}

		return new SeparatorNodes(continuation);
	}

}
