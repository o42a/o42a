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

import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class SentenceParser implements Parser<SentenceNode> {

	private static final MarkParser MARK = new MarkParser();

	private final Grammar grammar;

	public SentenceParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SentenceNode parse(ParserContext context) {

		final AlternativeNode[] disjunction =
				context.expect(MARK).parse(this.grammar.disjunction());

		if (disjunction == null) {

			final SeparatorNodes comments = context.skipComments(true);
			final SignNode<SentenceType> mark = context.parse(MARK);

			if (mark == null) {
				return null;
			}

			final SentenceNode sentenceNode =
					new SentenceNode(new AlternativeNode[0], mark);

			sentenceNode.addComments(comments);

			return sentenceNode;
		}

		final SignNode<SentenceType> mark = context.parse(MARK);
		final SentenceNode sentence = new SentenceNode(disjunction, mark);

		if (mark != null) {
			return sentence;
		}

		return context.acceptComments(true, sentence);
	}

	private static final class MarkParser
			implements Parser<SignNode<SentenceType>> {

		@Override
		public SignNode<SentenceType> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final SentenceType sentenceType;

			switch(context.next()) {
			case '.':
				sentenceType = SentenceType.PROPOSITION;
				break;
			case '!':
				sentenceType = SentenceType.CLAIM;
				break;
			case '?':
				sentenceType = SentenceType.ISSUE;
				break;
			default:
				return null;
			}

			context.acceptAll();

			final SignNode<SentenceType> result = new SignNode<>(
					start,
					context.current().fix(),
					sentenceType);

			return context.acceptComments(true, result);
		}

	}

}
