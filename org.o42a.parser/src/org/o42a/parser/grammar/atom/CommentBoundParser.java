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

import static org.o42a.ast.atom.CommentBound.BLOCK_COMMENT;
import static org.o42a.ast.atom.CommentBound.INLINE_COMMENT;

import org.o42a.ast.atom.CommentBound;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class CommentBoundParser implements Parser<SignNode<CommentBound>> {

	static final CommentBoundParser COMMENT_BOUND =
			new CommentBoundParser();
	static final CommentBoundParser INLINE_BOUND =
			new CommentBoundParser(INLINE_COMMENT);
	static final CommentBoundParser BLOCK_BOUND =
			new CommentBoundParser(BLOCK_COMMENT);

	private final CommentBound bound;
	private final int minLength;

	private CommentBoundParser() {
		this.bound = null;
		this.minLength = 2;
	}

	private CommentBoundParser(CommentBound bound) {
		this.bound = bound;
		this.minLength = bound.getSign().length();
	}

	@Override
	public SignNode<CommentBound> parse(ParserContext context) {
		if (context.next() != '~') {
			return null;
		}

		final boolean startsAtNewLine = context.isLineStart();
		int len = 1;
		final SourcePosition start = context.current().fix();

		for (;;) {
			if (context.next() == '~') {
				++len;
				continue;
			}
			if (len < this.minLength) {
				return null;
			}
			break;
		}

		context.acceptButLast();

		return new SignNode<>(
				start,
				context.firstUnaccepted().fix(),
				bound(len, startsAtNewLine));
	}

	private CommentBound bound(int len, boolean startsAtNewLine) {
		if (this.bound != null) {
			return this.bound;
		}
		if (!startsAtNewLine || len < 3) {
			return INLINE_COMMENT;
		}
		return BLOCK_COMMENT;
	}

}
