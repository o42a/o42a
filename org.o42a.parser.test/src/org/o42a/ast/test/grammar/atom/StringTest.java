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
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.StringBound.DOUBLE_QUOTE;
import static org.o42a.ast.atom.StringBound.SINGLE_QUOTE;

import org.junit.Before;
import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;


public class StringTest extends GrammarTestCase {

	private Parser<StringNode> parser;

	@Before
	public void setup() {
		this.parser = Grammar.string();
	}

	@Test
	public void emptySingleQuoted() {

		final StringNode string = parse("''");

		assertThat(string.getText(), is(""));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(
				string.getClosingBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(string, hasRange(0, 2));
		assertThat(string.getOpeningBound(), hasRange(0, 1));
		assertThat(string.getClosingBound(), hasRange(1, 2));
	}

	@Test
	public void emptyDoubleQuoted() {

		final StringNode string = parse("\"\"");

		assertThat(string.getText(), is(""));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTE));
		assertThat(
				string.getClosingBound().getType(),
				is(DOUBLE_QUOTE));
		assertThat(string, hasRange(0, 2));
		assertThat(string.getOpeningBound(), hasRange(0, 1));
		assertThat(string.getClosingBound(), hasRange(1, 2));
	}

	@Test
	public void singleQuoted() {

		final StringNode string = parse("'abc'");

		assertThat(string.getText(), is("abc"));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(
				string.getClosingBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(string, hasRange(0, 5));
		assertThat(string.getOpeningBound(), hasRange(0, 1));
		assertThat(string.getClosingBound(), hasRange(4, 5));
	}

	@Test
	public void doubleQuoted() {

		final StringNode string = parse("\"abc\"");

		assertThat(string.getText(), is("abc"));
		assertThat(
				string.getOpeningBound().getType(),
				is(DOUBLE_QUOTE));
		assertThat(
				string.getClosingBound().getType(),
				is(DOUBLE_QUOTE));
		assertThat(string, hasRange(0, 5));
		assertThat(string.getOpeningBound(), hasRange(0, 1));
		assertThat(string.getClosingBound(), hasRange(4, 5));
	}

	@Test
	public void escape() {
		assertThat(parse("'\\n'").getText(), is("\n"));
		assertThat(parse("'\\r'").getText(), is("\r"));
		assertThat(parse("'\\t'").getText(), is("\t"));
	}

	@Test
	public void unicodeEscape() {

		final StringNode string = parse("'\\f1C\\'");

		assertThat(string.getText().codePointAt(0), is(0xf1c));
		assertThat(
				string.getOpeningBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(
				string.getClosingBound().getType(),
				is(SINGLE_QUOTE));
		assertThat(string, hasRange(0, 7));
		assertThat(string.getOpeningBound(), hasRange(0, 1));
		assertThat(string.getClosingBound(), hasRange(6, 7));
	}

	private StringNode parse(String text) {
		return parse(this.parser, text);
	}

}
