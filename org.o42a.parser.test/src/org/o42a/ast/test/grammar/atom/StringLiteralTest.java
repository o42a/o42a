/*
    Parser Tests
    Copyright (C) 2010 Ruslan Lopatin

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

import org.junit.Before;
import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;


public class StringLiteralTest extends GrammarTestCase {

	private Parser<StringNode> parser;

	@Before
	public void setup() {
		this.parser = Grammar.stringLiteral();
	}

	@Test
	public void emptySingleQuoted() {

		final StringNode string = parse("''");

		assertEquals("", string.getText());
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getClosingQuotationMark().getType());
		assertRange(0, 2, string);
		assertRange(0, 1, string.getOpeningQuotationMark());
		assertRange(1, 2, string.getClosingQuotationMark());
	}

	@Test
	public void emptyDoubleQuoted() {

		final StringNode string = parse("\"\"");

		assertEquals("", string.getText());
		assertEquals(
				StringNode.DOUBLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.DOUBLE_QUOTE,
				string.getClosingQuotationMark().getType());
		assertRange(0, 2, string);
		assertRange(0, 1, string.getOpeningQuotationMark());
		assertRange(1, 2, string.getClosingQuotationMark());
	}

	@Test
	public void singleQuoted() {

		final StringNode string = parse("'abc'");

		assertEquals("abc", string.getText());
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getClosingQuotationMark().getType());
		assertRange(0, 5, string);
		assertRange(0, 1, string.getOpeningQuotationMark());
		assertRange(4, 5, string.getClosingQuotationMark());
	}

	@Test
	public void doubleQuoted() {

		final StringNode string = parse("\"abc\"");

		assertEquals("abc", string.getText());
		assertEquals(
				StringNode.DOUBLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.DOUBLE_QUOTE,
				string.getClosingQuotationMark().getType());
		assertRange(0, 5, string);
		assertRange(0, 1, string.getOpeningQuotationMark());
		assertRange(4, 5, string.getClosingQuotationMark());
	}

	@Test
	public void escape() {
		assertEquals("\n", parse("'\\n'").getText());
		assertEquals("\r", parse("'\\r'").getText());
		assertEquals("\t", parse("'\\t'").getText());
	}

	@Test
	public void unicodeEscape() {

		final StringNode string = parse("'\\f1C\\'");

		assertEquals(0xf1c, string.getText().charAt(0));
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getOpeningQuotationMark().getType());
		assertEquals(
				StringNode.SINGLE_QUOTE,
				string.getClosingQuotationMark().getType());
		assertRange(0, 7, string);
		assertRange(0, 1, string.getOpeningQuotationMark());
		assertRange(6, 7, string.getClosingQuotationMark());
	}

	private StringNode parse(String text) {
		return parse(this.parser, text);
	}

}
