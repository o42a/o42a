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

import static org.o42a.parser.Grammar.ref;
import static org.o42a.parser.grammar.statement.FlowOperatorParser.FLOW_OPERATOR;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.FlowOperator;
import org.o42a.ast.statement.PassThroughNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class PassThroughParser implements Parser<PassThroughNode> {

	public static final PassThroughParser PASS_THROUGH =
			new PassThroughParser(null);

	private final ExpressionNode input;

	public PassThroughParser(ExpressionNode input) {
		this.input = input;
	}

	@Override
	public PassThroughNode parse(ParserContext context) {

		final SignNode<FlowOperator> operator = context.push(FLOW_OPERATOR);

		if (operator == null) {
			return null;
		}

		final RefNode flow = context.parse(ref());

		if (flow == null) {
			if (this.input != null) {
				return null;
			}
			context.acceptAll();
			context.getLogger().error(
					"missing_flow",
					context.current(),
					"The flow to pass the input through is missing");
		}

		return new PassThroughNode(this.input, operator, flow);
	}

}
