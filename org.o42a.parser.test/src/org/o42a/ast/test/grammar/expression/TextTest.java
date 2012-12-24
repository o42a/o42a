/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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

		assertThat(text, hasRange(0, 5));
		assertEquals(1, text.getLiterals().length);
		assertThat(text.getLiterals()[0], hasRange(0, 5));
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

		assertThat(text, hasRange(0, 5));
		assertEquals(1, text.getLiterals().length);
		assertThat(text.getLiterals()[0], hasRange(0, 5));
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

		assertThat(text, hasRange(0, 11));
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
