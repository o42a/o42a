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
package org.o42a.ast.test.grammar.atom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.o42a.ast.Position;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class MultiLineStringLiteralTest extends GrammarTestCase {

	@Test
	public void emptySingleQuoted() {

		final StringNode string = parse("\\''\\");

		assertEquals("", string.getText());
		assertEquals(
				StringNode.MULTILINE_SINGLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.MULTILINE_SINGLE_QUOTE.getClosing(),
				string.getClosingQuotationMark().getType());
		assertRange(0, 4, string);
		assertRange(0, 2, string.getOpeningQuotationMark());
		assertRange(2, 4, string.getClosingQuotationMark());
	}

	@Test
	public void emptyDoubleQuoted() {

		final StringNode string = parse("\\\"\"\\");

		assertEquals("", string.getText());
		assertEquals(
				StringNode.MULTILINE_DOUBLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.MULTILINE_DOUBLE_QUOTE.getClosing(),
				string.getClosingQuotationMark().getType());
		assertRange(0, 4, string);
		assertRange(0, 2, string.getOpeningQuotationMark());
		assertRange(2, 4, string.getClosingQuotationMark());
	}

	@Test
	public void singleQuoted() {

		final StringNode string = parse("\\'abc'\\");

		assertEquals("abc", string.getText());
		assertEquals(
				StringNode.MULTILINE_SINGLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.MULTILINE_SINGLE_QUOTE.getClosing(),
				string.getClosingQuotationMark().getType());
		assertRange(0, 7, string);
		assertRange(0, 2, string.getOpeningQuotationMark());
		assertRange(5, 7, string.getClosingQuotationMark());
	}

	@Test
	public void doubleQuoted() {

		final StringNode string = parse("\\\"abc\"\\");

		assertEquals("abc", string.getText());
		assertEquals(
				StringNode.MULTILINE_DOUBLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.MULTILINE_DOUBLE_QUOTE.getClosing(),
				string.getClosingQuotationMark().getType());
		assertRange(0, 7, string);
		assertRange(0, 2, string.getOpeningQuotationMark());
		assertRange(5, 7, string.getClosingQuotationMark());
	}

	@Test
	public void multiline() {

		final StringNode string = parse(
			"\\'abc\n"
			+ "def'\\");

		assertEquals("abc\ndef", string.getText());
	}

	@Test
	public void stripEmptyFirstLine() {

		final StringNode string = parse(
				"\\'   \n"
				+ "abc'\\");

		assertEquals("abc", string.getText());
	}

	@Test
	public void stripEmptyLastLine() {

		final StringNode string = parse(
				"\\'abc\n"
				+ "   '\\");

		assertEquals("abc", string.getText());
	}

	@Test
	public void stripTrailingWhitespace() {

		final StringNode string = parse(
				"\\'abc  \n"
				+ "def'\\");

		assertEquals("abc\ndef", string.getText());
	}

	@Test
	public void dontStripTrailingWhitespaceAtLastLine() {

		final StringNode string = parse(
				"\\'abc  \n"
				+ "def  '\\");

		assertEquals("abc\ndef  ", string.getText());

		final Position position = this.worker.position();

		assertEquals(15, position.offset());
		assertEquals(2, position.line());
		assertEquals(8, position.column());
	}

	@Test
	public void multiline2() {

		final StringNode string = parse(
				"\\'  \n"
				+ " abc  \n"
				+ " ' def  \n"
				+ "   '\\");

		assertEquals(" abc\n ' def", string.getText());
	}

	@Test
	public void trailingQuote() {

		final StringNode string = parse("\\'''''\\");

		assertEquals("'''", string.getText());
	}

	private StringNode parse(String text) {
		return parse(Grammar.stringLiteral(), text);
	}

}
