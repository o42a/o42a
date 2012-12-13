/*
    Parser Tests
    Copyright (C) 2012 Ruslan Lopatin

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
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BinaryPrecedenceTest extends GrammarTestCase {

	@Test
	public void leadingPrecedesSuffix() {
		checkPrecedence(
				parse("a / b ~ c"),
				BinaryOperator.DIVIDE,
				false,
				BinaryOperator.SUFFIX);
	}

	@Test
	public void suffixPrecedesLeading() {
		checkPrecedence(
				parse("a ~ b / c"),
				BinaryOperator.SUFFIX,
				false,
				BinaryOperator.DIVIDE);
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
			boolean leftToRight,
			BinaryOperator innerOperator) {
		assertThat(outer.getOperator(), is(outerOperator));

		final BinaryNode inner = to(
				BinaryNode.class,
				leftToRight ? outer.getLeftOperand() : outer.getRightOperand());

		assertThat(inner.getOperator(), is(innerOperator));
	}

	private BinaryNode parse(String text) {
		return to(BinaryNode.class, parse(expression(), text));
	}

}
