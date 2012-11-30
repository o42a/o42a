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
