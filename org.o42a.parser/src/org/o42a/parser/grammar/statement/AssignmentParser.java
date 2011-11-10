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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.IMPERATIVE;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.AssignmentNode.AssignmentOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class AssignmentParser implements Parser<AssignmentNode> {

	private final ExpressionNode destination;

	public AssignmentParser(ExpressionNode destination) {
		this.destination = destination;
	}

	@Override
	public AssignmentNode parse(ParserContext context) {
		if (context.next() != '=') {
			return null;
		}

		final SourcePosition operatorStart = context.current().fix();

		context.acceptAll();

		final SignNode<AssignmentOperator> operator =
				new SignNode<AssignmentOperator>(
						operatorStart,
						context.current().fix(),
						AssignmentOperator.ASSIGN);

		context.acceptComments(false, operator);

		final ExpressionNode value = context.parse(IMPERATIVE.expression());

		if (value == null) {
			context.getLogger().missingValue(operator);
		}

		return new AssignmentNode(this.destination, operator, value);
	}

}
