/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BinaryPrecedenceTest extends GrammarTestCase {

	@Test
	public void suffixPrecedesDivide() {
		checkPrecedence(
				parse("a / b ~ c"),
				BinaryOperator.DIVIDE,
				false,
				BinaryOperator.SUFFIX);
		checkPrecedence(
				parse("a ~ b / c"),
				BinaryOperator.DIVIDE,
				true,
				BinaryOperator.SUFFIX);
	}

	@Test
	public void macrosImplicitSuffixPrecedesDivide() {
		checkPrecedence(
				parse("a / b ##c [d]"),
				BinaryOperator.DIVIDE,
				false,
				BinaryOperator.SUFFIX);
		checkPrecedence(
				parse("a ##b [d] / c"),
				BinaryOperator.DIVIDE,
				true,
				BinaryOperator.SUFFIX);
	}

	@Test
	public void multiplyPrecedesAdd() {
		checkPrecedence(
				parse("a + b * c"),
				BinaryOperator.ADD,
				false,
				BinaryOperator.MULTIPLY);
		checkPrecedence(
				parse("a * b + c"),
				BinaryOperator.ADD,
				true,
				BinaryOperator.MULTIPLY);
	}

	@Test
	public void dividePrecedesLess() {
		checkPrecedence(
				parse("a < b / c"),
				BinaryOperator.LESS,
				false,
				BinaryOperator.DIVIDE);
		checkPrecedence(
				parse("a / b < c"),
				BinaryOperator.LESS,
				true,
				BinaryOperator.DIVIDE);
	}

	@Test
	public void comparePrecedesGreater() {
		checkPrecedence(
				parse("0 > a <=> b"),
				BinaryOperator.GREATER,
				false,
				BinaryOperator.COMPARE);
		checkPrecedence(
				parse("a <=> b > c"),
				BinaryOperator.GREATER,
				true,
				BinaryOperator.COMPARE);
	}

	@Test
	public void leftPrecedesRight() {
		checkPrecedence(
				parse("a + b - c"),
				BinaryOperator.SUBTRACT,
				true,
				BinaryOperator.ADD);
	}

	private static void checkPrecedence(
			BinaryNode outer,
			BinaryOperator outerOperator,
			boolean innerLeft,
			BinaryOperator innerOperator) {
		assertThat(outer.getOperator(), is(outerOperator));

		final BinaryNode inner = to(
				BinaryNode.class,
				innerLeft ? outer.getLeftOperand() : outer.getRightOperand());

		assertThat(inner.getOperator(), is(innerOperator));
	}

	private BinaryNode parse(String text) {
		return to(BinaryNode.class, parse(expression(), text));
	}

}
