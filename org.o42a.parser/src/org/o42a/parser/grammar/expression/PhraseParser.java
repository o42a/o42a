/*
    Parser
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import java.util.ArrayList;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ClauseNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class PhraseParser implements Parser<PhraseNode> {

	private final ExpressionNode prefix;
	private final Grammar grammar;

	public PhraseParser(Grammar grammar, ExpressionNode prefix) {
		this.grammar = grammar;
		this.prefix = prefix;
	}

	@Override
	public PhraseNode parse(ParserContext context) {

		final ClauseParser clauseParser = new ClauseParser(this.grammar);
		final ArrayList<ClauseNode> clauses = new ArrayList<ClauseNode>();

		for (;;) {

			final ClauseNode clause = context.parse(clauseParser);

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

		private final Grammar grammar;
		private boolean precedingName;

		ClauseParser(Grammar grammar) {
			this.grammar = grammar;
		}

		@Override
		public ClauseNode parse(ParserContext context) {

			final int c = context.next();

			switch (c) {
			case '[':
				this.precedingName = false;
				return context.parse(this.grammar.brackets());
			case '(':
				this.precedingName = false;
				return context.parse(DECLARATIVE.parentheses());
			case '{':
				this.precedingName = false;
				return context.parse(braces());
			case '\\':
			case '\'':
			case '"':
				this.precedingName = false;
				return context.parse(text());
			default:
				if (this.precedingName) {
					return null;
				}

				final NameNode name = context.parse(name());

				if (name == null) {
					return null;
				}

				this.precedingName = true;

				return context.acceptComments(name);
			}
		}

	}

}
