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
package org.o42a.parser.grammar.sentence;

import static org.o42a.ast.sentence.SentenceType.*;
import static org.o42a.parser.Grammar.name;
import static org.o42a.util.string.Characters.HORIZONTAL_ELLIPSIS;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.*;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class SentenceParser implements Parser<SentenceNode> {

	private static final MarkParser MARK = new MarkParser();
	private static final ContinuedMarkParser CONTINUED_MARK =
			new ContinuedMarkParser();
	private static final ContinuationParser SENTENCE_CONTINUATION =
			new ContinuationParser();

	private final Grammar grammar;

	public SentenceParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SentenceNode parse(ParserContext context) {

		final AlternativeNode[] disjunction =
				context.expect(MARK).parse(this.grammar.disjunction());
		final SeparatorNodes comments;

		if (disjunction == null) {
			comments = context.skipComments(true);
		} else {
			comments = null;
		}

		final SignNode<SentenceType> mark = context.parse(MARK);
		final ContinuationNode continuation;

		if (mark == null) {
			if (disjunction == null) {
				return null;
			}
			continuation = null;
		} else if (mark.getType().supportsContinuation()) {
			continuation = context.parse(SENTENCE_CONTINUATION);
		} else {
			continuation = null;
		}

		final SentenceNode sentence = new SentenceNode(
				disjunction != null ? disjunction : new AlternativeNode[0],
				mark,
				continuation);

		if (comments != null) {
			sentence.addComments(comments);
		}

		return context.acceptComments(true, sentence);
	}

	private static final class MarkParser
			implements Parser<SignNode<SentenceType>> {

		@Override
		public SignNode<SentenceType> parse(ParserContext context) {

			final SentenceType sentenceType = guessSentenceType(context);

			if (sentenceType == null) {
				return null;
			}

			final SignNode<SentenceType> mark =
					parseMark(context, sentenceType);

			return context.acceptComments(false, mark);
		}

		private SentenceType guessSentenceType(ParserContext context) {
			switch(context.next()) {
			case '.':
				return DECLARATION;
			case '!':
				return EXCLAMATION;
			case '?':
				return INTERROGATION;
			case HORIZONTAL_ELLIPSIS:
				return CONTINUATION;
			default:
				return null;
			}
		}

		private SignNode<SentenceType> parseMark(
				ParserContext context,
				SentenceType sentenceType) {

			final SourcePosition start = context.current().fix();

			if (sentenceType == CONTINUATION) {
				context.acceptAll();
				return new SignNode<>(
						start,
						context.current().fix(),
						sentenceType);
			}

			final SignNode<SentenceType> continued =
					context.parse(CONTINUED_MARK);

			if (continued != null) {
				return continued;
			}

			context.acceptAll();

			return new SignNode<>(
					start,
					context.current().fix(),
					sentenceType);
		}

	}

	private static final class ContinuedMarkParser
			implements Parser<SignNode<SentenceType>> {

		@Override
		public SignNode<SentenceType> parse(ParserContext context) {

			final SentenceType sentenceType = predictSentenceType(context);

			if (sentenceType == null) {
				return null;
			}

			final SourcePosition start = context.current().fix();

			if (context.next() != '.') {
				return null;
			}
			if (context.next() != '.') {
				return null;
			}

			if (sentenceType == CONTINUATION) {
				context.acceptAll();
			} else {
				if (context.next() == '.') {
					return null;
				}
				context.acceptButLast();
			}

			return new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					sentenceType);
		}

		private SentenceType predictSentenceType(ParserContext context) {
			switch (context.next()) {
			case '?':
				return CONTINUED_INTERROGATION;
			case '!':
				return CONTINUED_EXCLAMATION;
			case '.':
				return CONTINUATION;
			default:
				return null;
			}
		}

	}

	private static final class ContinuationParser
			implements Parser<ContinuationNode> {

		@Override
		public ContinuationNode parse(ParserContext context) {

			final NameNode label = context.parse(name());

			if (label == null) {
				return null;// No label - no continuation
			}

			context.skipComments(false, label);

			final SignNode<SentenceType> period;

			// The label should be followed either by period, or by new line.
			switch (context.next()) {
			case '.':
				// Period? Can be ellipsis too.
				final SignNode<SentenceType> mark = context.push(MARK);

				if (mark.getType() != DECLARATION) {
					// Not a period. Ellipsis probably.
					return null;
				}

				period = mark;
				context.acceptAll();
				break;
			case '\n':
				context.acceptAll();
				period = null;
				break;
			default:
				if (context.current().line() > label.getEnd().getLine()) {
					// New line.
					context.acceptButLast();
					period = null;
					break;
				}
				return null;
			}

			return new ContinuationNode(label, period);
		}

	}

}
