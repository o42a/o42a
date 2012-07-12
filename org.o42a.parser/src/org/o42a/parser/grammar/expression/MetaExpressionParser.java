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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.simpleExpression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MetaExpressionNode;
import org.o42a.ast.expression.MetaExpressionNode.Prefix;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MetaExpressionParser implements Parser<MetaExpressionNode> {

	public static final MetaExpressionParser META_EXPRESSION =
			new MetaExpressionParser();

	private MetaExpressionParser() {
	}

	@Override
	public MetaExpressionNode parse(ParserContext context) {
		if (context.next() != '#') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<Prefix> prefix = context.acceptComments(
				false,
				new SignNode<MetaExpressionNode.Prefix>(
						start,
						context.current().fix(),
						MetaExpressionNode.Prefix.HASH));

		final ExpressionNode macro = context.parse(simpleExpression());

		if (macro == null) {
			context.getLogger().error(
					"missing_macro",
					prefix,
					"Missing macro expression to substitute");
		}

		return new MetaExpressionNode(prefix, macro);
	}

}
