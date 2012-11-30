/*
    Parser
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.parser.grammar.clause;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class ClauseIdParser implements Parser<ClauseIdNode> {

	public static final ClauseIdParser CLAUSE_ID = new ClauseIdParser();

	private ClauseIdParser() {
	}

	@Override
	public ClauseIdNode parse(ParserContext context) {
		switch (context.next()) {
		case '@':
			return context.parse(declarableAdapter());
		case '[':
			return context.parse(brackets());
		case '{':
			return context.parse(braces());
		case '+':
		case '-':
			return context.parse(unary());
		case '\'':
			return context.parse(stringLiteral());
		}

		final RefNode ref = context.parse(ref());

		if (ref == null) {
			return null;
		}

		final BinaryNode binary = context.parse(new BinaryClauseIdParser(ref));

		if (binary != null) {
			return binary;
		}

		final PhraseNode phrase = context.parse(phrase(ref));

		if (phrase != null) {
			return phrase;
		}

		if (ref instanceof ClauseIdNode) {
			return (ClauseIdNode) ref;
		}

		return null;
	}

}
