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

import java.util.ArrayList;

import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class ContentParser implements Parser<SentenceNode[]> {

	private final Grammar grammar;

	public ContentParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SentenceNode[] parse(ParserContext context) {

		final ArrayList<SentenceNode> sentences = new ArrayList<>();

		for (;;) {

			final SentenceNode sentence =
					context.parse(this.grammar.sentence());

			if (sentence == null) {
				break;
			}
			sentences.add(sentence);
		}

		final int size = sentences.size();

		if (size == 0) {
			return null;
		}

		return sentences.toArray(new SentenceNode[size]);
	}

}
