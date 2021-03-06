/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class PhraseTest extends GrammarTestCase {

	@Test
	public void call() {

		final PhraseNode result = parse("foo(bar)");

		assertThat(result.getPrefix(), isName("foo"));

		assertThat(singleStatement(
		MemberRefNode.class,
		singlePhrasePart(ParenthesesNode.class, result)), isName("bar"));
	}

	@Test
	public void nlAfterCall() {

		final PhraseNode result = parse(
				"foo()",
				"bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(1));
	}

	@Test
	public void continuationAfterCall() {

		final PhraseNode result = parse(
				"foo()",
				"_ bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(2));
	}

	@Test
	public void imperative() {

		final PhraseNode result = parse("foo{bar}");

		assertThat(result.getPrefix(), isName("foo"));

		assertThat(singleStatement(
		MemberRefNode.class,
		singlePhrasePart(BracesNode.class, result)), isName("bar"));
	}

	@Test
	public void nlAfterImperative() {

		final PhraseNode result = parse(
				"foo{}",
				"bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(1));
	}

	@Test
	public void continuationAfterImperative() {

		final PhraseNode result = parse(
				"foo{}",
				"_ bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(2));
	}

	@Test
	public void value() {

		final PhraseNode result = parse("foo[bar]");

		assertThat(result.getPrefix(), isName("foo"));

		final ArgumentNode[] arguments =
				singlePhrasePart(BracketsNode.class, result).getArguments();

		assertEquals(1, arguments.length);
		assertThat(arguments[0].getValue(), isName("bar"));
	}

	@Test
	public void nlAfterValue() {

		final PhraseNode result = parse(
				"foo[]",
				"bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(1));
	}

	@Test
	public void continuationAfterValue() {

		final PhraseNode result = parse(
				"foo[]",
				"_ bar");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(2));
	}

	@Test
	public void string() {

		final PhraseNode result = parse("foo 'bar' 'baz'");

		assertThat(result.getPrefix(), isName("foo"));

		final StringNode[] literals =
				singlePhrasePart(TextNode.class, result).getLiterals();

		assertEquals(2, literals.length);

		assertEquals("bar", literals[0].getText());
		assertEquals("baz", literals[1].getText());
	}

	@Test
	public void nlAfterStrinf() {

		final PhraseNode result = parse(
				"foo 'bar'",
				"baz");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(1));
	}

	@Test
	public void continuationAfterString() {

		final PhraseNode result = parse(
				"foo 'bar'",
				"_ baz");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(2));
	}

	@Test
	public void name() {

		final PhraseNode result = parse("foo [bar] baz");

		assertThat(result.getPrefix(), isName("foo"));

		final ArgumentNode[] arguments =
				phrasePart(BracketsNode.class, result, 0, 2).getArguments();

		assertThat(arguments.length, is(1));
		assertThat(arguments[0].getValue(), isName("bar"));

		final NameNode name = phrasePart(NameNode.class, result, 1, 2);

		assertThat(canonicalName(name), is("baz"));
	}

	@Test
	public void argumentAfterName() {

		final PhraseNode result = parse("foo _ bar [baz]");

		assertThat(result.getPrefix(), isName("foo"));

		final NameNode name = phrasePart(NameNode.class, result, 0, 2);

		assertThat(canonicalName(name), is("bar"));

		final ArgumentNode[] arguments =
				phrasePart(BracketsNode.class, result, 1, 2).getArguments();

		assertThat(arguments.length, is(1));
		assertThat(arguments[0].getValue(), isName("baz"));
	}

	@Test
	public void names() {

		final PhraseNode result = parse("foo_bar_baz");

		assertThat(result.getPrefix(), isName("foo"));

		final NameNode name1 = phrasePart(NameNode.class, result, 0, 2);

		assertThat(canonicalName(name1), is("bar"));

		final NameNode name2 = phrasePart(NameNode.class, result, 1, 2);

		assertThat(canonicalName(name2), is("baz"));
	}

	@Test
	public void nlAfterName() {

		final PhraseNode result = parse(
				"foo _ bar",
				"(baz)");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(1));
	}

	@Test
	public void continuationAfterName() {

		final PhraseNode result = parse(
				"foo ",
				"_ bar",
				"_ (baz)");

		assertThat(result.getPrefix(), isName("foo"));
		assertThat(result.getParts().length, is(2));
	}

	@Test
	public void integerAfterName() {

		final PhraseNode result = parse("foo_ 42");

		assertThat(result.getPrefix(), isName("foo"));

		final PhrasePartNode[] parts = result.getParts();

		assertThat(parts.length, is(1));
		assertThat(parts[0], is(unsignedInteger("42")));
	}

	@Test
	public void integerAfterText() {

		final PhraseNode result = parse("foo 'bar' 'baz' 42");

		assertThat(result.getPrefix(), isName("foo"));

		final PhrasePartNode[] parts = result.getParts();

		assertThat(parts.length, is(2));
		to(TextNode.class, parts[0]);
		assertThat(parts[1], is(unsignedInteger("42")));
	}

	@Test
	public void decimalAfterCall() {

		final PhraseNode result = parse("foo (bar) 123 456");

		assertThat(result.getPrefix(), isName("foo"));

		final PhrasePartNode[] parts = result.getParts();

		assertThat(parts.length, is(2));
		to(ParenthesesNode.class, parts[0]);
		assertThat(parts[1], is(unsignedInteger("123456")));
	}

	private PhraseNode parse(String... lines) {
		return to(PhraseNode.class, parseLines(expression(), lines));
	}

}
