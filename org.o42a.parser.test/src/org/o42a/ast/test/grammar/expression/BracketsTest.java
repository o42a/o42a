/*
    Parser Tests
    Copyright (C) 2011 Ruslan Lopatin

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BracketsTest extends GrammarTestCase {

	@Test
	public void empty() {

		final BracketsNode brackets = parse("[]");

		assertThat(brackets.getArguments().length, is(0));
	}

	@Test
	public void singleValue() {

		final BracketsNode brackets = parse("[foo]");

		assertThat(brackets.getArguments().length, is(1));
		assertName("foo", brackets.getArguments()[0].getValue());
	}

	@Test
	public void multipleValues() {

		final BracketsNode brackets = parse("[foo, bar, baz]");

		assertThat(brackets.getArguments().length, is(3));
		assertName("foo", brackets.getArguments()[0].getValue());
		assertName("bar", brackets.getArguments()[1].getValue());
		assertName("baz", brackets.getArguments()[2].getValue());
	}

	@Test
	public void skipFirst() {

		final BracketsNode brackets = parse("[, bar, baz]");

		assertThat(brackets.getArguments().length, is(3));
		assertNull(brackets.getArguments()[0].getValue());
		assertName("bar", brackets.getArguments()[1].getValue());
		assertName("baz", brackets.getArguments()[2].getValue());
	}

	@Test
	public void skipSecond() {

		final BracketsNode brackets = parse("[foo, , baz]");

		assertThat(brackets.getArguments().length, is(3));
		assertName("foo", brackets.getArguments()[0].getValue());
		assertNull(brackets.getArguments()[1].getValue());
		assertName("baz", brackets.getArguments()[2].getValue());
	}

	@Test
	public void skipLast() {

		final BracketsNode brackets = parse("[foo, bar,]");

		assertThat(brackets.getArguments().length, is(3));
		assertName("foo", brackets.getArguments()[0].getValue());
		assertName("bar", brackets.getArguments()[1].getValue());
		assertNull(brackets.getArguments()[2].getValue());
	}

	@Test
	public void skipSeveral() {

		final BracketsNode brackets = parse("[,,]");

		assertThat(brackets.getArguments().length, is(3));
		assertNull(brackets.getArguments()[0].getValue());
		assertNull(brackets.getArguments()[1].getValue());
		assertNull(brackets.getArguments()[2].getValue());
	}

	@Test
	public void newLine() {

		final BracketsNode brackets = parse(
				"[",
				"  a()",
				"  b",
				"  c",
				"]");

		assertThat(brackets.getArguments().length, is(3));
		assertName(
				"a",
				to(
						PhraseNode.class,
						brackets.getArguments()[0].getValue()).getPrefix());
		assertName("b", brackets.getArguments()[1].getValue());
		assertName("c", brackets.getArguments()[2].getValue());
	}


	private BracketsNode parse(String... lines) {
		return parseLines(DECLARATIVE.brackets(), lines);
	}

}
