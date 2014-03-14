/*
    Parser
    Copyright (C) 2014 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.name;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.PassThroughNode;
import org.o42a.ast.statement.PassThroughNode.Operator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class PassThroughParser implements Parser<PassThroughNode> {

	public static final PassThroughParser PASS_THROUGH =
			new PassThroughParser(null);

	private static final OperatorParser OPERATOR = new OperatorParser();

	private final ExpressionNode input;

	public PassThroughParser(ExpressionNode input) {
		this.input = input;
	}

	@Override
	public PassThroughNode parse(ParserContext context) {

		final SignNode<Operator> operator = context.parse(OPERATOR);

		if (operator == null) {
			return null;
		}

		final NameNode flow = context.parse(name());

		if (flow == null) {
			context.getLogger().error(
					"missing_flow",
					context.current(),
					"The flow to pass the input through is missing");
		}

		return context.acceptComments(
				false,
				new PassThroughNode(this.input, operator, flow));
	}

	private static final class OperatorParser
			implements Parser<SignNode<PassThroughNode.Operator>> {

		@Override
		public SignNode<PassThroughNode.Operator> parse(ParserContext context) {
			if (context.next() != '>') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			if (context.next() != '>') {
				return null;
			}
			if (context.next() == '>') {
				return null;
			}

			context.acceptButLast();

			return context.acceptComments(
					false,
					new SignNode<>(
							start,
							context.current().fix(),
							PassThroughNode.Operator.CHEVRON));
		}

	}

}
