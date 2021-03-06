/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.name;

import org.junit.Test;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class NameTest extends GrammarTestCase {

	@Test
	public void simpleName() {

		final NameNode name = parse("abc");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 3));
		assertThat(name.getName().toString(), is("abc"));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void whitespaceSeparatedName() {

		final NameNode name = parse("a c");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 3));
		assertThat(name.getName().toString(), is("a c"));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void number() {

		final NameNode name = parse("a3b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 3));
		assertThat(name.getName().toString(), is("a3b"));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void numberAfterNumber() {

		final NameNode name = parse("a3 4b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 5));
		assertThat(name.getName().toString(), is("a3 4b"));
		assertThat(this.worker.position().offset(), is(5L));
	}

	@Test
	public void startFromNumber() {

		final NameNode name = parse("3a");

		assertThat(name, nullValue());
		assertThat(this.worker.position().offset(), is(0L));
	}

	@Test
	public void hyphen() {

		final NameNode name = parse("a-b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 3));
		assertThat(name.getName().toString(), is("a-b"));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void spaceAfterHyphenMinus() {

		final NameNode name = parse("a- b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 4));
		assertThat(name.getName().toString(), is("a-b"));
		assertThat(this.worker.position().offset(), is(4L));
	}

	@Test
	public void newLine() {

		final NameNode name = parse("a\n b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 1));
		assertThat(name.getName().toString(), is("a"));
		assertThat(this.worker.position().offset(), is(1L));
	}

	@Test
	public void spaceAfterHyphen() {

		final NameNode name = parse("a\u2010 b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 4));
		assertThat(name.getName().toString(), is("a-b"));
		assertThat(this.worker.position().offset(), is(4L));
	}

	@Test
	public void whitespaceAfterNonBreakingHyphen() {
		expectError("discouraging_whitespace");

		final NameNode name = parse("a\u2011 b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 4));
		assertThat(name.getName().toString(), is("a-b"));
		assertThat(this.worker.position().offset(), is(4L));
	}

	@Test
	public void hyphenFirst() {

		final NameNode name = parse("-a");

		assertThat(name, nullValue());
	}

	@Test
	public void hyphenLast() {

		final NameNode name = parse("a-");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 1));
		assertThat(name.getName().toString(), is("a"));
		assertThat(this.worker.position().offset(), is(1L));
	}

	@Test
	public void hyphenMinusAfterWhitespace() {

		final NameNode name = parse("a -");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 1));
		assertThat(name.getName().toString(), is("a"));
		assertThat(this.worker.position().offset(), is(1L));
	}

	@Test
	public void hyphenAfterWhitespace() {
		expectError("discouraging_whitespace");

		final NameNode name = parse("a \u2010 b");

		assertThat(name.getName().toString(), is("a-b"));
	}

	@Test
	public void nonBreakingHyphenAfterWhitespace() {
		expectError("discouraging_whitespace");

		final NameNode name = parse("a \u2011b");

		assertThat(name.getName().toString(), is("a-b"));
	}

	@Test
	public void hyphenAfterHyphen() {

		final NameNode name = parse("a--b");

		assertThat(name, notNullValue());
		assertThat(name, hasRange(0, 1));
		assertThat(name.getName().toString(), is("a"));
		assertThat(this.worker.position().offset(), is(1L));
	}

	private NameNode parse(String text) {
		return parse(name(), text);
	}

}
