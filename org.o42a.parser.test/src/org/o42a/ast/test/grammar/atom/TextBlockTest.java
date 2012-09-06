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
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.StringBound.DOUBLE_QUOTED_LINE;
import static org.o42a.ast.atom.StringBound.SINGLE_QUOTED_LINE;
import static org.o42a.parser.Grammar.stringLiteral;

import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class TextBlockTest extends GrammarTestCase {

	@Test
	public void emptySingleQuoted() {

		final StringNode string = parse(
				"''''",
				"'''");

		assertThat(string.getText(), is(""));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTED_LINE));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTED_LINE));
		assertThat(string, hasRange(0, 9));
		assertThat(string.getOpeningBound(), hasRange(0, 5));
		assertThat(string.getClosingBound(), hasRange(5, 9));
	}

	@Test
	public void emptyDoubleQuoted() {

		final StringNode string = parse(
				"\"\"\"\"",
				"\"\"\"");

		assertThat(string.getText(), is(""));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTED_LINE));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTED_LINE));
		assertThat(string, hasRange(0, 9));
		assertThat(string.getOpeningBound(), hasRange(0, 5));
		assertThat(string.getClosingBound(), hasRange(5, 9));
	}

	@Test
	public void singleQuoted() {

		final StringNode string = parse(
				"'''  ",
				"abc",
				"  '''  ");

		assertThat(string.getText(), is("abc"));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTED_LINE));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTED_LINE));
		assertThat(string.getOpeningBound().getStart().getLine(), is(1));
		assertThat(string.getClosingBound().getEnd().getLine(), is(4));
	}

	@Test
	public void doubleQuoted() {

		final StringNode string = parse(
				"\"\"\"  ",
				"abc",
				"  \"\"\"  ");

		assertThat(string.getText(), is("abc"));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTED_LINE));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTED_LINE));
		assertThat(string.getOpeningBound().getStart().getLine(), is(1));
		assertThat(string.getClosingBound().getEnd().getLine(), is(4));
	}

	@Test
	public void multiline() {

		final StringNode string = parse(
				"''''  ",
				"abc",
				"def",
				"   '''   ");

		assertEquals("abc\ndef", string.getText());
	}

	@Test
	public void dontStripTrailingWhitespaceAtLastLine() {

		final StringNode string = parse(
				"''''  ",
				"abc",
				"",
				"   '''   ");

		assertEquals("abc\n", string.getText());
	}

	@Test
	public void textAfterQuotes() {

		final StringNode string = parse(
				"''''  ",
				"abc",
				"'''  def",
				"   '''   ");

		assertEquals("abc\n'''  def", string.getText());
	}

	@Test
	public void textBeforeQuotes() {

		final StringNode string = parse(
				"''''  ",
				"abc",
				"def ''' ",
				"   '''   ");

		assertEquals("abc\ndef '''", string.getText());
	}

	private StringNode parse(String... text) {
		return parseLines(stringLiteral(), text);
	}

}
