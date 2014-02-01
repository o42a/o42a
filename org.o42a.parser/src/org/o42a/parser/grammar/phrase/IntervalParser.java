/*
    Parser
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.phrase;

import static org.o42a.parser.Grammar.separator;
import static org.o42a.parser.grammar.phrase.BoundParser.BOUND;
import static org.o42a.util.string.Characters.HORIZONTAL_ELLIPSIS;

import org.o42a.ast.atom.HorizontalEllipsis;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.phrase.IntervalBracket;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public final class IntervalParser implements Parser<IntervalNode> {

	public static final IntervalParser INTERVAL = new IntervalParser();

	private static final LeftBracketParser LEFT_BRACKET =
			new LeftBracketParser();
	private static final RightBracketParser RIGHT_BRACKET =
			new RightBracketParser();
	private static final EllipsisParser ELLIPSIS = new EllipsisParser();

	private IntervalParser() {
	}

	@Override
	public IntervalNode parse(ParserContext context) {

		final SignNode<IntervalBracket> leftBracket =
				context.push(LEFT_BRACKET);

		if (leftBracket == null) {
			return null;
		}

		final BoundNode leftBound = context.push(BOUND);
		final SignNode<HorizontalEllipsis> ellipsis = context.parse(ELLIPSIS);

		if (ellipsis == null) {
			return null;
		}

		BoundNode rightBound = null;
		SourcePosition firstUnexpected = null;

		// An ellipsis present.
		// Find the right bound and the - the right bracket.
		// Report all unexpected text as syntax errors.
		for (;;) {

			final SourcePosition start = context.current().fix();

			final SeparatorNodes separators = context.parse(separator(true));

			if (separators != null) {
				// Skip separators. Store comments.
				if (firstUnexpected != null && !separators.isWhitespace()) {
					context.logUnexpected(firstUnexpected, start);
					firstUnexpected = null;
				}
				if (rightBound != null) {
					rightBound.addComments(separators);
				} else {
					ellipsis.addComments(separators);
				}
			}

			// Attempt to parse the right bound if not known yet.
			if (rightBound == null) {
				rightBound = context.parse(BOUND);
				if (rightBound != null) {
					if (firstUnexpected != null) {
						context.logUnexpected(firstUnexpected, start);
						firstUnexpected = null;
					}
				}
			}

			// Attempt to find the right bracket.
			final SignNode<IntervalBracket> rightBracket =
					context.parse(RIGHT_BRACKET);

			if (rightBracket != null) {
				// The right bracket found.
				if (firstUnexpected != null) {
					context.logUnexpected(firstUnexpected, start);
				}
				return new IntervalNode(
						leftBracket,
						leftBound,
						ellipsis,
						rightBound,
						rightBracket);
			}
			if (context.isEOF()) {
				// Report the EOF.
				if (firstUnexpected != null) {
					context.logUnexpected(firstUnexpected, start);
				}
				context.getLogger().eof(context.current().fix());
				return new IntervalNode(
						leftBracket,
						leftBound,
						ellipsis,
						rightBound,
						null);
			}

			if (firstUnexpected == null) {
				// Unexpected text start.
				firstUnexpected = context.current().fix();
			}
			context.next();
		}
	}

	private static final class LeftBracketParser
			implements Parser<SignNode<IntervalBracket>> {

		@Override
		public SignNode<IntervalBracket> parse(ParserContext context) {

			final IntervalBracket bracket;

			switch (context.next()) {
			case '(':
				bracket = IntervalBracket.LEFT_OPEN_BRACKET;
				break;
			case '[':
				bracket = IntervalBracket.LEFT_CLOSED_BRACKET;
				break;
			default:
				return null;
			}

			final SourcePosition start = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					true,
					new SignNode<>(start, context.current().fix(), bracket));
		}

	}

	private static final class RightBracketParser
			implements Parser<SignNode<IntervalBracket>> {

		@Override
		public SignNode<IntervalBracket> parse(ParserContext context) {

			final IntervalBracket bracket;

			switch (context.next()) {
			case ')':
				bracket = IntervalBracket.RIGHT_OPEN_BRACKET;
				break;
			case ']':
				bracket = IntervalBracket.RIGHT_CLOSED_BRACKET;
				break;
			default:
				return null;
			}

			final SourcePosition start = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					false,
					new SignNode<>(start, context.current().fix(), bracket));
		}

	}

	private static final class EllipsisParser
			implements Parser<SignNode<HorizontalEllipsis>> {

		@Override
		public SignNode<HorizontalEllipsis> parse(ParserContext context) {

			final SourcePosition start;

			switch (context.next()) {
			case HORIZONTAL_ELLIPSIS:
				start = context.current().fix();
				break;
			case '.':
				start = context.current().fix();
				if (context.next() != '.') {
					return null;
				}
				if (context.next() != '.') {
					return null;
				}
				break;
			default:
				return null;
			}

			context.acceptAll();

			return context.acceptComments(
					true,
					new SignNode<>(
							start,
							context.current().fix(),
							HorizontalEllipsis.HORIZONTAL_ELLIPSIS));
		}

	}

}
