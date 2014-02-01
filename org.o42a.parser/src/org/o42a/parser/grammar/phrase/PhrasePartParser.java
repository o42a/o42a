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

import static org.o42a.parser.Grammar.*;
import static org.o42a.parser.grammar.phrase.TypeDefinitionParser.TYPE_DEFINITION;
import static org.o42a.util.string.Characters.isDigit;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class PhrasePartParser implements Parser<PhrasePartNode> {

	static final PhrasePartParser PHRASE_PART = new PhrasePartParser();

	private PhrasePartParser() {
	}

	@Override
	public PhrasePartNode parse(ParserContext context) {

		final int c = context.next();

		switch (c) {
		case '[':

			final IntervalNode leftClosedInterval = context.parse(interval());

			if (leftClosedInterval != null) {
				return leftClosedInterval;
			}

			return context.parse(brackets());
		case '(':

			final IntervalNode leftOpenInterval = context.parse(interval());

			if (leftOpenInterval != null) {
				return leftOpenInterval;
			}

			return context.parse(DECLARATIVE.parentheses());
		case '{':
			return context.parse(braces());
		case '#':
			return context.parse(TYPE_DEFINITION);
		case '\\':
		case '\'':
		case '"':
			return context.parse(text());
		default:
			if (isDigit(c)) {
				return context.parse(number());
			}

			final NameNode name = context.parse(name());

			if (name == null) {
				return null;
			}

			return context.acceptComments(false, name);
		}
	}

}
