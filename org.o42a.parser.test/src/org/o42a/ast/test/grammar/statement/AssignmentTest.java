/*
    Parser Tests
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
package org.o42a.ast.test.grammar.statement;

import static org.junit.Assert.assertEquals;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class AssignmentTest extends GrammarTestCase {

	@Test
	public void assignment() {

		final AssignmentNode assignment = parse("foo = bar");

		assertRange(0, 9, assignment);
		assertName("foo", assignment.getDestination());
		assertName("bar", assignment.getValue());
	}

	@Test
	public void binaryExpressionValue() {

		final AssignmentNode assignment = parse("foo = bar + baz");

		assertName("foo", assignment.getDestination());

		final BinaryNode value =
				to(BinaryNode.class, assignment.getValue());

		assertEquals(BinaryOperator.ADD, value.getOperator());
		assertName("bar", value.getLeftOperand());
		assertName("baz", value.getRightOperand());
	}

	@Test
	public void binaryExpressionDestination() {

		final AssignmentNode assignment = parse("foo + bar = baz");
		final BinaryNode destination =
				to(BinaryNode.class, assignment.getDestination());

		assertEquals(BinaryOperator.ADD, destination.getOperator());
		assertName("foo", destination.getLeftOperand());
		assertName("bar", destination.getRightOperand());
		assertName("baz", assignment.getValue());
	}

	private AssignmentNode parse(String text) {
		return to(AssignmentNode.class, parse(IMPERATIVE.statement(), text));
	}

}
