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

import static org.o42a.ast.atom.CommentBound.INLINE_COMMENT;
import static org.o42a.parser.grammar.atom.CommentBoundParser.COMMENT_BOUND;

import org.o42a.ast.atom.CommentBound;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class NewLineCommentParser implements Parser<CommentNode> {

	public static final NewLineCommentParser NL_COMMENT =
			new NewLineCommentParser();

	private NewLineCommentParser() {
	}

	@Override
	public CommentNode parse(ParserContext context) {

		final SignNode<CommentBound> opening = context.parse(COMMENT_BOUND);

		if (opening == null) {
			return null;
		}
		if (context.isEOF()) {
			return new CommentNode(
					opening,
					"",
					context.firstUnaccepted().fix());
		}

		if (!opening.getType().isBlock()) {
			return inlineComment(context, opening);
		}

		final CommentNode blockComment =
				context.parse(new BlockCommentParser(opening));

		if (blockComment != null) {
			return blockComment;
		}

		// Not a block comment despite the number of opening tildes.
		return inlineComment(
				context,
				new SignNode<>(
						opening.getStart(),
						opening.getEnd(),
						INLINE_COMMENT));
	}

	private CommentNode inlineComment(
			ParserContext context,
			SignNode<CommentBound> opening) {
		return context.parse(new InlineCommentParser(opening, true));
	}

}
