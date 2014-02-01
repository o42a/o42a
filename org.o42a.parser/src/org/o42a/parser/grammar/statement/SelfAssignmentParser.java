/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.ast.statement.SelfAssignmentNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class SelfAssignmentParser implements Parser<SelfAssignmentNode> {

	public static final SelfAssignmentParser SELF_ASSIGNMENT =
			new SelfAssignmentParser();

	private SelfAssignmentParser() {
	}

	@Override
	public SelfAssignmentNode parse(ParserContext context) {
		if (context.next() != '=') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<AssignmentOperator> prefix = new SignNode<>(
				start,
				context.current().fix(),
				AssignmentOperator.ASSIGN);

		if (context.next() == '=') {
			return null;
		}

		context.acceptButLast();
		context.acceptComments(false, prefix);

		final ExpressionNode value = context.parse(expression());

		if (value == null) {
			context.getLogger().missingValue(context.current());
		}

		return new SelfAssignmentNode(prefix, value);
	}

}
