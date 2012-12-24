/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.StringBound.DOUBLE_QUOTED_LINE;
import static org.o42a.ast.atom.StringBound.SINGLE_QUOTED_LINE;
import static org.o42a.parser.Grammar.string;

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
		return parseLines(string(), text);
	}

}
