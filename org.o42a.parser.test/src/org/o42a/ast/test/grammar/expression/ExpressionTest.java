/*
    Parser Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;


public class ExpressionTest extends GrammarTestCase {

	@Test
	public void unaryExpressionOnCall() {

		final UnaryNode result =
				to(UnaryNode.class, parse("+foo()"));

		assertEquals(UnaryOperator.PLUS, result.getOperator());

		final PhraseNode operand = to(PhraseNode.class, result.getOperand());

		assertThat(operand.getPrefix(), isName("foo"));
	}

	@Test
	public void ascendantsAsLeftOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("foo & bar + baz"));
		final AscendantsNode left =
				to(AscendantsNode.class, result.getLeftOperand());

		assertThat(left.getAncestor().getSpec(), isName("foo"));
		assertThat(left.getSamples()[0].getSpec(), isName("bar"));
		assertThat(result.getRightOperand(), isName("baz"));
	}

	@Test
	public void samplesAsLeftOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("&foo & bar + baz"));
		final AscendantsNode left =
				to(AscendantsNode.class, result.getLeftOperand());

		assertThat(left.getAncestor().getSpec(), isName("foo"));
		assertThat(left.getSamples()[0].getSpec(), isName("bar"));
		assertThat(result.getRightOperand(), isName("baz"));
	}

	@Test
	public void ascendantsAsRigntOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("foo \u2212 bar & baz"));

		assertThat(result.getLeftOperand(), isName("foo"));

		final AscendantsNode right =
				to(AscendantsNode.class, result.getRightOperand());

		assertThat(right.getAncestor().getSpec(), isName("bar"));
		assertThat(right.getSamples()[0].getSpec(), isName("baz"));
	}

	@Test
	public void samplesAsRightOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("foo  + &bar & baz"));
		final AscendantsNode right =
				to(AscendantsNode.class, result.getRightOperand());

		assertThat(result.getLeftOperand(), isName("foo"));
		assertThat(right.getAncestor().getSpec(), isName("bar"));
		assertThat(right.getSamples()[0].getSpec(), isName("baz"));
	}

	@Test
	public void unaryPrecedence() {
		assertUnaries(
				UnaryOperator.IS_TRUE,
				UnaryOperator.PLUS,
				"foo",
				parse("+++foo"));
		assertUnaries(
				UnaryOperator.PLUS,
				UnaryOperator.IS_TRUE,
				"foo",
				parse("+ ++foo"));
		assertUnaries(
				UnaryOperator.NOT,
				UnaryOperator.MINUS,
				"foo",
				parse("---foo"));
		assertUnaries(
				UnaryOperator.MINUS,
				UnaryOperator.NOT,
				"foo",
				parse("- --foo"));
	}

	@Test
	public void binaryMetaExpression() {
		to(BinaryNode.class, parse("#a + b"));
		to(BinaryNode.class, parse("a + #b"));
		to(BinaryNode.class, parse("#a + #b"));
	}

	private static void assertUnaries(
			UnaryOperator first,
			UnaryOperator second,
			String name,
			ExpressionNode expression) {

		final UnaryNode unary1 = to(UnaryNode.class, expression);

		assertEquals(first, unary1.getOperator());

		final UnaryNode unary2 = to(UnaryNode.class, unary1.getOperand());

		assertEquals(second, unary2.getOperator());

		assertThat(unary2.getOperand(), isName(name));
	}

	private ExpressionNode parse(String text) {
		return parse(expression(), text);
	}

}
