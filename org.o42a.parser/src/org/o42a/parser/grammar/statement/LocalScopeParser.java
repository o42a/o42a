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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.braces;
import static org.o42a.parser.Grammar.local;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.*;
import org.o42a.ast.statement.LocalScopeNode.Separator;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class LocalScopeParser implements Parser<LocalScopeNode> {

	private static final SeparatorParser SEPARATOR = new SeparatorParser();

	private final Grammar grammar;
	private final ExpressionNode expression;
	private final LocalNode local;

	public LocalScopeParser(Grammar grammar) {
		this.grammar = grammar;
		this.expression = null;
		this.local = null;
	}

	public LocalScopeParser(Grammar grammar, ExpressionNode expression) {
		this.grammar = grammar;
		this.expression = expression;
		this.local = null;
	}

	public LocalScopeParser(Grammar grammar, LocalNode local) {
		this.grammar = grammar;
		this.expression = null;
		this.local = local;
	}

	@Override
	public LocalScopeNode parse(ParserContext context) {

		final LocalNode local = parseLocal(context);

		if (local == null) {
			return null;
		}

		final SignNode<LocalScopeNode.Separator> separator;
		final StatementNode content;

		switch (context.next()) {
		case ':':
			separator = context.parse(SEPARATOR);
			content = context.parse(this.grammar.localStatement());
			break;
		case '(':
			separator = null;
			content = context.parse(this.grammar.parentheses());
			break;
		case '{':
			separator = null;
			content = context.parse(braces());
			break;
		default:
			return null;
		}

		return new LocalScopeNode(local, separator, content);
	}

	private LocalNode parseLocal(ParserContext context) {
		if (this.local != null) {
			return this.local;
		}
		if (this.expression != null) {
			return context.parse(new LocalParser(this.expression));
		}
		return context.parse(local());
	}

	private static final class SeparatorParser
			implements Parser<SignNode<LocalScopeNode.Separator>> {

		@Override
		public SignNode<Separator> parse(ParserContext context) {
			if (context.next() != ':') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					true,
					new SignNode<>(
							start,
							context.current().fix(),
							LocalScopeNode.Separator.COLON));
		}

	}

}
