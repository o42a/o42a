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
package org.o42a.parser.grammar.statement;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class SentenceParser implements Parser<SentenceNode> {

	private static final MarkParser MARK = new MarkParser();

	private final Grammar grammar;

	public SentenceParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SentenceNode parse(ParserContext context) {

		final AlternativeNode[] disjunction =
			context.parse(this.grammar.disjunction());

		if (disjunction == null) {
			return null;
		}

		final SignNode<SentenceType> mark = context.parse(MARK);
		final SentenceNode sentence = new SentenceNode(disjunction, mark);

		sentence.addComments(context.acceptComments());

		return sentence;
	}

	private static final class MarkParser
			implements Parser<SignNode<SentenceType>> {

		@Override
		public SignNode<SentenceType> parse(ParserContext context) {

			final FixedPosition start = context.current().fix();
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

			final SignNode<SentenceType> result =
				new SignNode<SentenceType>(start, context.current(), sentenceType);

			return result;
		}

	}

}
