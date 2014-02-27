/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.sentence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.sentence.SentenceType.*;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ContinuationTast extends GrammarTestCase {

	@Test
	public void continuation() {

		final SentenceNode sentence = parseOne("foo...");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation(null, false));
	}

	@Test
	public void continuationByHorizontalEllipsis() {

		final SentenceNode sentence = parseOne("foo \u2026");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation(null, false));
	}

	@Test
	public void continuationWithLabel() {

		final SentenceNode sentence = parseOne(
				"foo ~~comment~~ ... label ~~comment",
				"~~comment~~");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation("label", false));
	}

	@Test
	public void continuationWithLabelAndPeriod() {

		final SentenceNode sentence = parseOne(
				"foo ~~comment~~ ... label ~~comment",
				"_.~~comment~~");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation("label", true));
	}

	@Test
	public void onlyContinuation() {

		final SentenceNode sentence = parseOne("...");

		assertThat(sentence.getDisjunction().length, is(0));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation(null, false));
	}

	@Test
	public void onlyContinuationWithLabel() {

		final SentenceNode sentence = parseOne("... label");

		assertThat(sentence.getDisjunction().length, is(0));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation("label", false));
	}

	@Test
	public void onlyContinuationWithLabelAndPeriod() {

		final SentenceNode sentence = parseOne("... label.");

		assertThat(sentence.getDisjunction().length, is(0));
		assertThat(sentence, sentenceOfType(CONTINUATION));
		assertThat(sentence, sentenceWithContinuation("label", true));
	}

	@Test
	public void continuedExcalamation() {

		final SentenceNode sentence = parseOne("foo!..");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_EXCLAMATION));
		assertThat(sentence, sentenceWithContinuation(null, false));
	}

	@Test
	public void continuedExclamationWithLabel() {

		final SentenceNode sentence = parseOne("foo!.. label");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_EXCLAMATION));
		assertThat(sentence, sentenceWithContinuation("label", false));
	}

	@Test
	public void continuedExclamationWithLabelAndPeriod() {

		final SentenceNode[] sentences = parseTwo("foo!.. label. bar");
		final SentenceNode sentence = sentences[0];

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_EXCLAMATION));
		assertThat(sentence, sentenceWithContinuation("label", true));
		assertThat(
				singleStatement(MemberRefNode.class, sentences[1]),
				isName("bar"));
	}

	@Test
	public void continuedInterrogation() {

		final SentenceNode sentence = parseOne("foo?..");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_INTERROGATION));
		assertThat(sentence, sentenceWithContinuation(null, false));
	}

	@Test
	public void continuedInterrogationWithLabel() {

		final SentenceNode sentence = parseOne("foo?.. label");

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_INTERROGATION));
		assertThat(sentence, sentenceWithContinuation("label", false));
	}

	@Test
	public void continuedInterrogationWithLabelAndPeriod() {

		final SentenceNode[] sentences = parseTwo("foo?.. label. bar");
		final SentenceNode sentence = sentences[0];

		assertThat(
				singleStatement(MemberRefNode.class, sentence),
				isName("foo"));
		assertThat(sentence, sentenceOfType(CONTINUED_INTERROGATION));
		assertThat(sentence, sentenceWithContinuation("label", true));
		assertThat(
				singleStatement(MemberRefNode.class, sentences[1]),
				isName("bar"));
	}

	@Test
	public void continuationAfterExclamation() {

		final SentenceNode[] sentences = parseTwo("foo!...bar");

		assertThat(sentences[0], sentenceOfType(EXCLAMATION));
		assertThat(sentences[0].getContinuation(), nullValue());
		assertThat(
				singleStatement(MemberRefNode.class, sentences[0]),
				isName("foo"));

		assertThat(sentences[1], sentenceOfType(CONTINUATION));
		assertThat(sentences[1].getDisjunction().length, is(0));
		assertThat(sentences[1], sentenceWithContinuation("bar", false));
	}

	@Test
	public void continuationAfterInterrogation() {

		final SentenceNode[] sentences = parseTwo("foo?...bar");

		assertThat(sentences[0], sentenceOfType(INTERROGATION));
		assertThat(sentences[0].getContinuation(), nullValue());
		assertThat(
				singleStatement(MemberRefNode.class, sentences[0]),
				isName("foo"));

		assertThat(sentences[1], sentenceOfType(CONTINUATION));
		assertThat(sentences[1].getDisjunction().length, is(0));
		assertThat(sentences[1], sentenceWithContinuation("bar", false));
	}

	@Test
	public void sentenceAfterContinuation() {

		final SentenceNode[] sentences = parseTwo(
				"...",
				"foo");

		assertThat(sentences[0].getDisjunction().length, is(0));
		assertThat(sentences[0], sentenceOfType(CONTINUATION));
		assertThat(sentences[0], sentenceWithContinuation(null, false));

		assertThat(
				singleStatement(MemberRefNode.class, sentences[1]),
				isName("foo"));
	}

	private SentenceNode parseOne(String...lines) {

		final SentenceNode[] result = parse(lines);

		assertThat("Exactly one sentence expected", result.length, is(1));

		return result[0];
	}

	private SentenceNode[] parseTwo(String...lines) {

		final SentenceNode[] result = parse(lines);

		assertThat("Exactly two sentences expected", result.length, is(2));

		return result;
	}

	private SentenceNode[] parse(String... lines) {
		return parseLines(IMPERATIVE.content(), lines);
	}

}
