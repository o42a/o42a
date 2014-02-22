/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.StaticRefNode;


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
	public void staticRefAsLeftOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("&foo + bar"));
		final StaticRefNode left =
				to(StaticRefNode.class, result.getLeftOperand());

		assertThat(left.getRef(), isName("foo"));
		assertThat(result.getRightOperand(), isName("bar"));
	}

	@Test
	public void staticRefAsRightOperand() {

		final BinaryNode result =
				to(BinaryNode.class, parse("foo  + &bar"));
		final StaticRefNode right =
				to(StaticRefNode.class, result.getRightOperand());

		assertThat(result.getLeftOperand(), isName("foo"));
		assertThat(right.getRef(), isName("bar"));
	}

	@Test
	public void binary() {
		to(BinaryNode.class, parse("a + b"));
		to(BinaryNode.class, parse("a - b"));
		to(BinaryNode.class, parse("a − b"));
		to(BinaryNode.class, parse("a * b"));
		to(BinaryNode.class, parse("a × b"));
		to(BinaryNode.class, parse("a ⋅ b"));
		to(BinaryNode.class, parse("a / b"));
		to(BinaryNode.class, parse("a ÷ b"));
		to(BinaryNode.class, parse("a ∕ b"));
		to(BinaryNode.class, parse("a == b"));
		to(BinaryNode.class, parse("a <> b"));
		to(BinaryNode.class, parse("a ≠ b"));
		to(BinaryNode.class, parse("a > b"));
		to(BinaryNode.class, parse("a >= b"));
		to(BinaryNode.class, parse("a ≥ b"));
		to(BinaryNode.class, parse("a < b"));
		to(BinaryNode.class, parse("a <= b"));
		to(BinaryNode.class, parse("a ≤ b"));
	}

	@Test
	public void binaryWithMacroExpansion() {
		to(BinaryNode.class, parse("#a + b"));
		to(BinaryNode.class, parse("a + #b"));
		to(BinaryNode.class, parse("#a + #b"));
	}

	@Test
	public void binaryWithGroup() {
		to(BinaryNode.class, parse("a\\ + b"));
		to(BinaryNode.class, parse("a + b\\"));
		to(BinaryNode.class, parse("a\\ + b\\"));
		to(BinaryNode.class, parse("a\\ f + b\\ g"));
	}

	private ExpressionNode parse(String text) {
		return parse(expression(), text);
	}

}
