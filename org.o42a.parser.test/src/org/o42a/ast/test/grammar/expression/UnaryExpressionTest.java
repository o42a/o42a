/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.unary;

import org.junit.Test;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class UnaryExpressionTest extends GrammarTestCase {

	@Test
	public void plus() {

		final UnaryNode result = parse("+foo");

		assertThat(result.getOperator(), is(UnaryOperator.PLUS));
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void hyphenMinus() {

		final UnaryNode result = parse("-foo");

		assertEquals(UnaryOperator.MINUS, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void minus() {

		final UnaryNode result = parse("\u2212foo");

		assertEquals(UnaryOperator.MINUS, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void not() {

		final UnaryNode result = parse("--foo");

		assertEquals(UnaryOperator.NOT, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 5));
		assertThat(result.getSign(), hasRange(0, 2));
	}

	@Test
	public void notSign() {

		final UnaryNode result = parse("\u00acfoo");

		assertEquals(UnaryOperator.NOT, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void isTrue() {

		final UnaryNode result = parse("++foo");

		assertEquals(UnaryOperator.IS_TRUE, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 5));
		assertThat(result.getSign(), hasRange(0, 2));
	}

	@Test
	public void valueOf() {

		final UnaryNode result = parse("\\foo");

		assertEquals(UnaryOperator.VALUE_OF, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void keepValue() {

		final UnaryNode result = parse("\\\\foo");

		assertEquals(UnaryOperator.KEEP_VALUE, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 5));
		assertThat(result.getSign(), hasRange(0, 2));
	}

	@Test
	public void link() {

		final UnaryNode result = parse("`foo");

		assertEquals(UnaryOperator.LINK, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void variable() {

		final UnaryNode result = parse("``foo");

		assertEquals(UnaryOperator.VARIABLE, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 5));
		assertThat(result.getSign(), hasRange(0, 2));
	}

	private UnaryNode parse(String text) {
		return parse(unary(), text);
	}

}
