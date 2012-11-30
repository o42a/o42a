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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.binaryExpression;
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
	public void hyphenSubtract() {
		assertOperator(BinaryOperator.SUBTRACT, "-");
	}

	@Test
	public void subtract() {
		assertOperator(BinaryOperator.SUBTRACT, "\u2212");
	}

	@Test
	public void multiply() {
		assertOperator(BinaryOperator.MULTIPLY, "*");
	}

	@Test
	public void divide() {
		assertOperator(BinaryOperator.DIVIDE, "/");
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
	}

	@Test
	public void less() {
		assertOperator(BinaryOperator.LESS, "<");
	}

	@Test
	public void lessOrEqual() {
		assertOperator(BinaryOperator.LESS_OR_EQUAL, "<=");
	}

	@Test
	public void greater() {
		assertOperator(BinaryOperator.GREATER, ">");
	}

	@Test
	public void greaterOrEqual() {
		assertOperator(BinaryOperator.GREATER_OR_EQUAL, ">=");
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

		return this.worker.parse(binaryExpression(expression));
	}

}
