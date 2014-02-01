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
package org.o42a.parser.grammar.phrase;

import static org.o42a.parser.grammar.phrase.PhrasePartParser.PHRASE_PART;

import java.util.ArrayList;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class PhraseParser implements Parser<PhraseNode> {

	private final ExpressionNode prefix;

	public PhraseParser(ExpressionNode prefix) {
		this.prefix = prefix;
	}

	@Override
	public PhraseNode parse(ParserContext context) {

		final ArrayList<PhrasePartNode> parts = new ArrayList<>();

		for (;;) {

			final PhrasePartNode part = context.parse(PHRASE_PART);

			if (part == null) {
				break;
			}

			parts.add(part);
		}

		final int size = parts.size();

		if (size == 0) {
			return null;
		}

		return new PhraseNode(
				this.prefix,
				parts.toArray(new PhrasePartNode[size]));
	}

}
