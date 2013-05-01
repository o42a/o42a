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
package org.o42a.parser.grammar.field;

import static org.o42a.ast.statement.AssignmentOperator.ASSIGN;
import static org.o42a.parser.Grammar.expression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class InitializerParser implements Parser<ExpressionNode> {

	public static final InitializerParser INITIALIZER = new InitializerParser();

	private InitializerParser() {
	}

	@Override
	public ExpressionNode parse(ParserContext context) {

		final ExpressionNode expression = context.parse(expression());

		if (expression == null) {
			return null;
		}
		if (context.next() != '=') {
			return expression;
		}

		final SignNode<AssignmentOperator> init = parseInit(context);
		final ExpressionNode value = context.parse(expression());

		if (value == null) {
			return expression;
		}

		return initializer(expression, init, value);
	}

	private static SignNode<AssignmentOperator> parseInit(
			ParserContext context) {

		final SourcePosition start = context.current().fix();

		context.skip();

		return context.skipComments(
				false,
				new SignNode<>(start, context.current().fix(), ASSIGN));
	}

	private static PhraseNode initializer(
			ExpressionNode expression,
			SignNode<AssignmentOperator> init,
			ExpressionNode value) {
		return new PhraseNode(
				expression,
				new BracketsNode(
						null,
						new ArgumentNode(null, init, value),
						null));
	}

}
