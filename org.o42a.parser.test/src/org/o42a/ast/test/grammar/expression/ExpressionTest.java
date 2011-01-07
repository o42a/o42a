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
package org.o42a.ast.test.grammar.expression;

import static org.junit.Assert.assertEquals;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ExpressionTest extends GrammarTestCase {

	@Test
	public void unaryExpressionOnCall() {

		final UnaryNode result =
			to(UnaryNode.class, parse("+foo()"));

		assertEquals(UnaryOperator.PLUS, result.getOperator());

		final PhraseNode operand = to(PhraseNode.class, result.getOperand());

		assertName("foo", operand.getPrefix());
	}

	@Test
	public void ascendantsAsLeftOperand() {

		final BinaryNode result =
			to(BinaryNode.class, parse("foo & bar + baz"));
		final AscendantsNode left =
			to(AscendantsNode.class, result.getLeftOperand());

		assertName("foo", left.getAscendants()[0].getAscendant());
		assertName("bar", left.getAscendants()[1].getAscendant());
		assertName("baz", result.getRightOperand());
	}

	@Test
	public void samplesAsLeftOperand() {

		final BinaryNode result =
			to(BinaryNode.class, parse("&foo & bar + baz"));
		final AscendantsNode left =
			to(AscendantsNode.class, result.getLeftOperand());

		assertName("foo", left.getAscendants()[0].getAscendant());
		assertName("bar", left.getAscendants()[1].getAscendant());
		assertName("baz", result.getRightOperand());
	}

	@Test
	public void ascendantsAsRigntOperand() {

		final BinaryNode result =
			to(BinaryNode.class, parse("foo + bar & baz"));

		assertName("foo", result.getLeftOperand());

		final AscendantsNode right =
			to(AscendantsNode.class, result.getRightOperand());

		assertName("bar", right.getAscendants()[0].getAscendant());
		assertName("baz", right.getAscendants()[1].getAscendant());
	}

	@Test
	public void samplesAsRightOperand() {

		final BinaryNode result =
			to(BinaryNode.class, parse("foo  + &bar & baz"));
		final AscendantsNode right =
			to(AscendantsNode.class, result.getRightOperand());

		assertName("foo", result.getLeftOperand());
		assertName("bar", right.getAscendants()[0].getAscendant());
		assertName("baz", right.getAscendants()[1].getAscendant());
	}

	private ExpressionNode parse(String text) {
		return parse(DECLARATIVE.expression(), text);
	}

}
