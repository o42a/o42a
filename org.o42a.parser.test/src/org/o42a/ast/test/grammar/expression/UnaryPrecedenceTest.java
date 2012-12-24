/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class UnaryPrecedenceTest extends GrammarTestCase {

	@Test
	public void isTruePrecedesPlus() {
		checkPrecedence(
				parse("+++foo"),
				UnaryOperator.IS_TRUE,
				UnaryOperator.PLUS,
				"foo");
	}

	@Test
	public void isTrueAfterPlus() {
		checkPrecedence(
				parse("+ ++foo"),
				UnaryOperator.PLUS,
				UnaryOperator.IS_TRUE,
				"foo");
	}

	@Test
	public void notPrecedesMinus() {
		checkPrecedence(
				parse("---foo"),
				UnaryOperator.NOT,
				UnaryOperator.MINUS,
				"foo");
	}

	@Test
	public void notAfterMinus() {
		checkPrecedence(
				parse("- --foo"),
				UnaryOperator.MINUS,
				UnaryOperator.NOT,
				"foo");
	}

	private static void checkPrecedence(
			UnaryNode outer,
			UnaryOperator outerOperator,
			UnaryOperator innerOperator,
			String name) {
		assertThat(outer.getOperator(), is(outerOperator));

		final UnaryNode inner = to(UnaryNode.class, outer.getOperand());

		assertThat(inner.getOperator(), is(innerOperator));
		assertThat(inner.getOperand(), isName(name));
	}

	private UnaryNode parse(String text) {
		return to(UnaryNode.class, parse(expression(), text));
	}

}
