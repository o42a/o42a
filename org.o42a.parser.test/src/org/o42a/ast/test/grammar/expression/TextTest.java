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
import static org.o42a.ast.atom.StringBound.DOUBLE_QUOTE;
import static org.o42a.ast.atom.StringBound.SINGLE_QUOTE;

import org.junit.Test;
import org.o42a.ast.expression.TextNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class TextTest extends GrammarTestCase {

	@Test
	public void singleQuotedString() {

		final TextNode text = parse("'foo'");

		assertRange(0, 5, text);
		assertEquals(1, text.getLiterals().length);
		assertRange(0, 5, text.getLiterals()[0]);
		assertThat(
				text.getLiterals()[0].getOpeningBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(
				text.getLiterals()[0].getClosingBound().getType(),
				is(SINGLE_QUOTE));
		assertEquals("foo", text.getText());
	}

	@Test
	public void doubleQuotedString() {

		final TextNode text = parse("\"foo\"");

		assertRange(0, 5, text);
		assertEquals(1, text.getLiterals().length);
		assertRange(0, 5, text.getLiterals()[0]);
		assertThat(
				text.getLiterals()[0].getOpeningBound().getType(),
				is(DOUBLE_QUOTE));
		assertThat(
				text.getLiterals()[0].getClosingBound().getType(),
				is(DOUBLE_QUOTE));
		assertEquals("foo", text.getText());
	}

	@Test
	public void twoLiteralsText() {

		final TextNode text = parse("'foo' 'bar'");

		assertRange(0, 11, text);
		assertEquals(2, text.getLiterals().length);
		assertEquals("foo", text.getLiterals()[0].getText());
		assertEquals("bar", text.getLiterals()[1].getText());
		assertEquals("foobar", text.getText());
	}

	@Test
	public void mixedLiteralsText() {

		final TextNode text = parse(
				"'foo' ~~ comment ~~ \"bar\"",
				"''''",
				"baz   ",
				"''''",
				"~~ comment");

		assertEquals(3, text.getLiterals().length);
		assertEquals("foo", text.getLiterals()[0].getText());
		assertEquals("bar", text.getLiterals()[1].getText());
		assertEquals("baz", text.getLiterals()[2].getText());
		assertEquals("foobarbaz", text.getText());
	}

	@Test
	public void nlAfterBlock() {

		final TextNode text = parse(
				"'''",
				"foo",
				"'''",
				"'bar'");

		assertThat(text.getLiterals().length, is(2));
		assertThat(text.getText(), is("foobar"));
	}

	@Test
	public void nlAfterString() {

		final TextNode text = parse(
				"'foo' ",
				" ''' ",
				"bar  ",
				" '''");

		assertThat(text.getLiterals().length, is(2));
		assertThat(text.getText(), is("foobar"));
	}

	private TextNode parse(String... text) {
		return parseLines(Grammar.text(), text);
	}

}
