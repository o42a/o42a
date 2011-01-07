/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import static org.junit.Assert.assertNotNull;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Before;
import org.junit.Test;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Parser;


public class UnaryExpressionTest extends GrammarTestCase {

	private Parser<UnaryNode> parser;

	@Before
	public void setup() {
		this.parser = DECLARATIVE.unaryExpression();
	}

	@Test
	public void plus() {

		final UnaryNode result = parse("+foo");

		assertNotNull(result);
		assertEquals(UnaryOperator.PLUS, result.getOperator());
		assertName("foo", result.getOperand());
		assertRange(0, 4, result);
		assertRange(0, 1, result.getSign());
	}

	@Test
	public void minus() {

		final UnaryNode result = parse("-foo");

		assertNotNull(result);
		assertEquals(UnaryOperator.MINUS, result.getOperator());
		assertName("foo", result.getOperand());
		assertRange(0, 4, result);
		assertRange(0, 1, result.getSign());
	}

	private UnaryNode parse(String text) {
		return parse(this.parser, text);
	}

}
