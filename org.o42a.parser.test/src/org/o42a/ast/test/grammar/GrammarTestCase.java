/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.ast.test.grammar.matchers.IntegerNumberNodeMatcher.INTEGER_NUMBER;
import static org.o42a.ast.test.grammar.matchers.MemberRefWithoutOwner.MEMBER_REF_WITHOUT_OWNER;
import static org.o42a.ast.test.grammar.matchers.MemberRefWithoutRetention.MEMBER_REF_WITHOUT_RETENTION;
import static org.o42a.util.string.NameEncoder.NAME_ENCODER;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.test.grammar.matchers.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.StringSource;
import org.o42a.util.string.Name;


public class GrammarTestCase {

	public static <T> T to(Class<? extends T> type, Object value) {
		assertThat("Value is null", value, notNullValue());
		assertTrue(
				value + " is of type " + value.getClass().getSimpleName()
				+ ", but " + type.getSimpleName() + " expected",
				type.isInstance(value));
		return type.cast(value);
	}

	public static String canonicalName(Name name) {
		assertThat("No name", name, notNullValue());
		return NAME_ENCODER.canonical().print(name);
	}

	public static String canonicalName(NameNode node) {
		return canonicalName(node.getName());
	}

	public static <T extends Node> Matcher<T> hasRange(long from, long to) {
		return new NodeRangeMatcher<>(from, to);
	}

	public static <T extends Node> Matcher<T> hasName(String name) {
		return new NodeHasNameMatcher<>(name);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> memberRefWithoutOwner() {
		return (Matcher<T>) MEMBER_REF_WITHOUT_OWNER;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> memberRefWithoutRetention() {
		return (Matcher<T>) MEMBER_REF_WITHOUT_RETENTION;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> isName(String name) {
		return CoreMatchers.<T>allOf(
				GrammarTestCase.<T>memberRefWithoutOwner(),
				GrammarTestCase.<T>memberRefWithoutRetention(),
				GrammarTestCase.<T>hasName(name));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> unsignedNumber() {
		return (Matcher<T>) NumberNodeSignMatcher.UNSIGNED_NUMBER;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> positiveNumber() {
		return (Matcher<T>) NumberNodeSignMatcher.POSITIVE_NUMBER;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> negativeNumber() {
		return (Matcher<T>) NumberNodeSignMatcher.NEGATIVE_NUMBER;
	}

	public static <T extends Node> Matcher<T> hasInteger(String digits) {
		return new NumberNodeHasIntegerMatcher<>(digits);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> isInteger() {
		return (Matcher<T>) INTEGER_NUMBER;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> integer(String digits) {
		return CoreMatchers.<T>allOf(
				GrammarTestCase.<T>hasInteger(digits),
				GrammarTestCase.<T>isInteger());
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> unsignedInteger(String digits) {
		return CoreMatchers.<T>allOf(
				GrammarTestCase.<T>unsignedNumber(),
				GrammarTestCase.<T>integer(digits));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> positiveInteger(String digits) {
		return CoreMatchers.<T>allOf(
				GrammarTestCase.<T>positiveNumber(),
				GrammarTestCase.<T>integer(digits));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> Matcher<T> negativeInteger(String digits) {
		return CoreMatchers.<T>allOf(
				GrammarTestCase.<T>negativeNumber(),
				GrammarTestCase.<T>integer(digits));
	}

	public static <T extends StatementNode> T singleStatement(
			Class<? extends T> type,
			BlockNode<?> block) {
		return singleStatement(type, singleSentence(block));
	}

	public static <T extends StatementNode> T singleStatement(
			Class<? extends T> type,
			SentenceNode sentence) {
		return statement(type, sentence, 0, 1);
	}

	public static <S extends SignType> S signType(SignNode<S> node) {
		return node == null ? null : node.getType();
	}

	public static SentenceNode singleSentence(BlockNode<?> block) {

		final SentenceNode[] sentences = block.getContent();

		assertEquals(
				"Single sentence expected: " + block.getContent(),
				1,
				sentences.length);

		return sentences[0];
	}

	public static <T extends StatementNode> T statement(
			Class<? extends T> type,
			BlockNode<?> block,
			int index,
			int length) {
		return statement(type, singleSentence(block), index, length);
	}

	public static <T extends StatementNode> T statement(
			Class<? extends T> type,
			SentenceNode sentence,
			int index,
			int length) {

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		assertEquals(
				"Single alternative expected: " + sentence,
				1,
				disjunction.length);


		final SerialNode[] conjunction = disjunction[0].getConjunction();

		assertEquals(
				"Wrong number of statements: " + sentence,
				length,
				conjunction.length);

		return to(type, conjunction[index].getStatement());
	}

	public static <T extends PhrasePartNode> T singlePhrasePart(
			Class<? extends T> type,
			PhraseNode phrase) {
		return phrasePart(type, phrase, 0, 1);
	}

	public static <T extends PhrasePartNode> T phrasePart(
			Class<? extends T> type,
			PhraseNode phrase,
			int index,
			int length) {

		final PhrasePartNode[] parts = phrase.getParts();

		assertEquals(
				"Wrong numer of phrase parts: " + phrase,
				length,
				parts.length);

		return to(type, parts[index]);
	}

	@Rule
	public final TestLogger logger = new TestLogger();

	protected ParserWorker worker;

	public void expectError(String code) {
		this.logger.expectError(code);
	}

	public <T> T parseLines(Parser<T> parser, String... lines) {

		final StringBuilder text = new StringBuilder();

		for (String line : lines) {
			text.append(line).append('\n');
		}

		return parse(parser, text.toString());
	}

	public <T> T parse(Parser<T> parser, String text) {
		this.worker = new ParserWorker(new StringSource(
				getClass().getSimpleName(),
				text.toString()));
		this.worker.setLogger(this.logger);
		return this.worker.parse(parser);
	}

}
