/*
    Parser
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.*;
import static org.o42a.parser.grammar.expression.TypeDefinitionParser.TYPE_DEFINITION;

import java.util.ArrayList;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class PhraseParser implements Parser<PhraseNode> {

	private static final ClauseParser CLAUSE = new ClauseParser();

	private final ExpressionNode prefix;

	public PhraseParser(ExpressionNode prefix) {
		this.prefix = prefix;
	}

	@Override
	public PhraseNode parse(ParserContext context) {

		final ArrayList<ClauseNode> clauses = new ArrayList<ClauseNode>();

		for (;;) {

			final ClauseNode clause = context.parse(CLAUSE);

			if (clause == null) {
				break;
			}

			clauses.add(clause);
		}

		final int size = clauses.size();

		if (size == 0) {
			return null;
		}

		return new PhraseNode(
				this.prefix,
				clauses.toArray(new ClauseNode[size]));
	}

	private static final class ClauseParser implements Parser<ClauseNode> {

		ClauseParser() {
		}

		@Override
		public ClauseNode parse(ParserContext context) {

			final int c = context.next();

			switch (c) {
			case '[':
				return context.parse(brackets());
			case '(':
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
					return context.parse(decimal());
				}

				final NameNode name = context.parse(name());

				if (name == null) {
					return null;
				}

				return context.acceptComments(false, name);
			}
		}

	}

}
