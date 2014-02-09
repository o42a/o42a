/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.sentence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.sentence.*;
import org.o42a.ast.sentence.AlternativeNode.Separator;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class SentenceTest extends GrammarTestCase {

	@Test
	public void sentence() {

		final SentenceNode sentence = parse("foo1, bar1; foo2, bar2");

		assertNotNull(sentence);
		assertEquals(SentenceType.DECLARATION, sentence.getType());
		assertNull(sentence.getMark());

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		assertEquals(2, disjunction.length);

		final AlternativeNode alt1 = disjunction[0];

		assertNull(alt1.getSeparator());
		assertEquals(2, alt1.getConjunction().length);
		assertThat(alt1.getConjunction()[0].getStatement(), isName("foo1"));
		assertThat(alt1.getConjunction()[1].getStatement(), isName("bar1"));

		final AlternativeNode alt2 = disjunction[1];

		assertEquals(Separator.ALTERNATIVE, alt2.getSeparator().getType());
		assertEquals(2, alt2.getConjunction().length);
		assertThat(alt2.getConjunction()[0].getStatement(), isName("foo2"));
		assertThat(alt2.getConjunction()[1].getStatement(), isName("bar2"));
	}

	@Test
	public void interrogation() {
		assertType(SentenceType.INTERROGATION, "foo?");
	}

	@Test
	public void declaration() {
		assertType(SentenceType.DECLARATION, "foo.");
	}

	@Test
	public void exclamation() {
		assertType(SentenceType.EXCLAMATION, "foo!");
	}

	@Test
	public void noSeparator() {
		assertType(SentenceType.DECLARATION, "foo");
	}

	@Test
	public void multiLineConjunction() {

		final SentenceNode result = parse(
				"a,",
				"b,",
				"c");

		assertThat(result.getDisjunction()[0].getConjunction().length, is(3));
	}

	@Test
	public void multiLineDisjunction() {

		final SentenceNode result = parse(
				"a;",
				"b;",
				"c");

		assertThat(result.getDisjunction().length, is(3));
	}

	@Test
	public void emptySentence() {

		final SentenceNode result = parse(" ");

		assertThat(result, nullValue());
	}

	@Test
	public void commentSentence() {

		final SentenceNode result = parse("~~ hello ~~");

		assertThat(result, nullValue());
	}

	@Test
	public void emptyProposition() {

		final SentenceNode result = parse(".");

		assertThat(result.getDisjunction().length, is(0));
	}

	@Test
	public void commentProposition() {

		final SentenceNode result = parse("~~ comment ~~.");

		assertThat(result.getDisjunction().length, is(0));
		assertThat(result.getComments().length, is(1));
	}

	private SentenceNode assertType(SentenceType type, String text) {

		final SentenceNode sentence = parse(text);

		assertEquals(type, sentence.getType());

		return sentence;
	}

	private SentenceNode parse(String... lines) {
		return parseLines(DECLARATIVE.sentence(), lines);
	}

}
