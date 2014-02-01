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

import static org.o42a.parser.Grammar.expression;
import static org.o42a.parser.Grammar.name;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.LocalNode.Separator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class LocalParser implements Parser<LocalNode> {

	public static final LocalParser LOCAL = new LocalParser();

	private final ExpressionNode expression;

	public LocalParser(ExpressionNode expression) {
		this.expression = expression;
	}

	private LocalParser() {
		this.expression = null;
	}

	@Override
	public LocalNode parse(ParserContext context) {

		final ExpressionNode expression = parseExpression(context);

		if (expression == null) {
			return null;
		}

		final SignNode<Separator> separator = parseSeparator(context);

		if (separator == null) {
			return null;
		}

		final NameNode name = context.parse(name());
		final LocalNode local = new LocalNode(expression, separator, name);

		if (name == null) {
			return local;
		}

		return context.acceptComments(false, local);
	}

	private ExpressionNode parseExpression(ParserContext context) {
		if (this.expression != null) {
			return this.expression;
		}
		return context.parse(expression());
	}

	private SignNode<Separator> parseSeparator(ParserContext context) {
		if (context.next() != '$') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		return context.acceptComments(
				false,
				new SignNode<>(
						start,
						context.current().fix(),
						LocalNode.Separator.DOLLAR_SIGN));
	}

}
