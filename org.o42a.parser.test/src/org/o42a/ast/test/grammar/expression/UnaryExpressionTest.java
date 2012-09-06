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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.unaryExpression;

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
	public void isTrue() {

		final UnaryNode result = parse("++foo");

		assertEquals(UnaryOperator.IS_TRUE, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 5));
		assertThat(result.getSign(), hasRange(0, 2));
	}

	@Test
	public void valueOf() {

		final UnaryNode result = parse("/foo");

		assertEquals(UnaryOperator.VALUE_OF, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	@Test
	public void macroExpansion() {

		final UnaryNode result = parse("#foo");

		assertEquals(UnaryOperator.MACRO_EXPANSION, result.getOperator());
		assertThat(result.getOperand(), isName("foo"));
		assertThat(result, hasRange(0, 4));
		assertThat(result.getSign(), hasRange(0, 1));
	}

	private UnaryNode parse(String text) {
		return parse(unaryExpression(), text);
	}

}
