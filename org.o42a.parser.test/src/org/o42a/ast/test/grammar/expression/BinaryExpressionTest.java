/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.binary;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.StringSource;


public class BinaryExpressionTest extends GrammarTestCase {

	@Test
	public void add() {
		assertOperator(BinaryOperator.ADD, "+");
	}

	@Test
	public void subtract() {
		assertOperator(BinaryOperator.SUBTRACT, "-");
		assertOperator(BinaryOperator.SUBTRACT, "\u2212");
	}

	@Test
	public void multiply() {
		assertOperator(BinaryOperator.MULTIPLY, "*");
		assertOperator(BinaryOperator.MULTIPLY, "\u00d7");
		assertOperator(BinaryOperator.MULTIPLY, "\u22c5");
	}

	@Test
	public void divide() {
		assertOperator(BinaryOperator.DIVIDE, "/");
		assertOperator(BinaryOperator.DIVIDE, "\u00f7");
		assertOperator(BinaryOperator.DIVIDE, "\u2215");
	}

	@Test
	public void compare() {
		assertOperator(BinaryOperator.COMPARE, "<=>");
	}

	@Test
	public void equal() {
		assertOperator(BinaryOperator.EQUAL, "==");
	}

	@Test
	public void notEqual() {
		assertOperator(BinaryOperator.NOT_EQUAL, "<>");
		assertOperator(BinaryOperator.NOT_EQUAL, "\u2260");
	}

	@Test
	public void less() {
		assertOperator(BinaryOperator.LESS, "<");
	}

	@Test
	public void lessOrEqual() {
		assertOperator(BinaryOperator.LESS_OR_EQUAL, "<=");
		assertOperator(BinaryOperator.LESS_OR_EQUAL, "\u2264");
	}

	@Test
	public void greater() {
		assertOperator(BinaryOperator.GREATER, ">");
	}

	@Test
	public void greaterOrEqual() {
		assertOperator(BinaryOperator.GREATER_OR_EQUAL, ">=");
		assertOperator(BinaryOperator.GREATER_OR_EQUAL, "\u2265");
	}

	@Test
	public void suffix() {
		assertOperator(BinaryOperator.SUFFIX, "~");
	}

	private void assertOperator(BinaryOperator operator, String sign) {

		final BinaryNode result = parse("foo " + sign + " bar ");

		assertThat(result, notNullValue());
		assertThat(result.getOperator(), is(operator));
		assertThat(result.getLeftOperand(), isName("foo"));
		assertThat(result.getRightOperand(), isName("bar"));
		assertEquals(0, result.getStart().getOffset());
		assertEquals(8 + sign.length(), result.getEnd().getOffset());
		assertEquals(4, result.getSign().getStart().getOffset());
		assertEquals(
				4 + sign.length(),
				result.getSign().getEnd().getOffset());
	}

	private BinaryNode parse(String text) {
		this.worker = new ParserWorker(
				new StringSource(getClass().getSimpleName(), text));

		final ExpressionNode expression =
				this.worker.parse(simpleExpression());

		return this.worker.parse(binary(expression));
	}

}
