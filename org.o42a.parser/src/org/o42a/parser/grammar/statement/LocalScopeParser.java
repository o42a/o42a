/*
    Parser
    Copyright (C) 2013 Ruslan Lopatin

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
import static org.o42a.parser.Grammar.iface;
import static org.o42a.parser.grammar.statement.LocalParser.LOCAL;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.LocalScopeNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class LocalScopeParser implements Parser<LocalScopeNode> {

	public static final LocalScopeParser DECLARATIVE_LOCAL_SCOPE =
			new LocalScopeParser(Grammar.DECLARATIVE);
	public static final LocalScopeParser IMPERATIVE_LOCAL_SCOPE =
			new LocalScopeParser(Grammar.IMPERATIVE);

	private final Grammar grammar;
	private final ExpressionNode expression;
	private final LocalNode local;


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

	private LocalScopeParser(Grammar grammar) {
		this.grammar = grammar;
		this.expression = null;
		this.local = null;
	}

	@Override
	public LocalScopeNode parse(ParserContext context) {

		final InterfaceNode iface;
		final LocalNode local;

		if (this.local != null) {
			iface = null;
			local = this.local;
		} else if (this.expression != null) {
			iface = null;
			local = context.parse(new LocalParser(this.expression));
			if (local == null) {
				return null;
			}
		} else {

			final int next = context.next();

			if (next == '`' || next == '(') {
				iface = context.parse(iface());
				if (iface == null) {
					return null;
				}
			} else {
				iface = null;
			}

			local = context.parse(LOCAL);
			if (local == null) {
				return null;
			}
		}

		final SignNode<LocalScopeNode.Separator> separator;
		final StatementNode content;
		final SourcePosition start = context.current().fix();

		switch (context.next()) {
		case ':':
			context.acceptAll();
			separator = context.acceptComments(
					true,
					new SignNode<>(
							start,
							context.current().fix(),
							LocalScopeNode.Separator.COLON));
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

		return new LocalScopeNode(iface, local, separator, content);
	}

}
