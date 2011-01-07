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

import org.junit.Test;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ConjunctionTest extends GrammarTestCase {

	@Test
	public void oneStatement() {

		final SerialNode[] result = parse("/* a */ foo // b ");

		assertNotNull(result);
		assertEquals(1, result.length);
		assertName("foo", result[0].getStatement());
	}

	@Test
	public void twoStatements() {

		final SerialNode[] result = parse("foo, bar");

		assertNotNull(result);
		assertEquals(2, result.length);
		assertName("foo", result[0].getStatement());
		assertName("bar", result[1].getStatement());
	}

	@Test
	public void threeStatements() {

		final SerialNode[] result = parse("foo, bar, bas");

		assertNotNull(result);
		assertEquals(3, result.length);
		assertName("foo", result[0].getStatement());
		assertName("bar", result[1].getStatement());
		assertName("bas", result[2].getStatement());
	}

	private SerialNode[] parse(String text) {
		return parse(DECLARATIVE.conjunction(), text);
	}

}
