/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.HorizontalEllipsis.HORIZONTAL_ELLIPSIS;
import static org.o42a.ast.phrase.IntervalBracket.*;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class IntervalTest extends GrammarTestCase {

	@Test
	public void closed() {

		final IntervalNode interval = parse("foo [a...b]");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_CLOSED_BRACKET));
	}

	@Test
	public void open() {

		final IntervalNode interval = parse("foo (a...b)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_OPEN_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void leftOpen() {

		final IntervalNode interval = parse("foo (a...b]");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_OPEN_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_CLOSED_BRACKET));
	}

	@Test
	public void rightOpen() {

		final IntervalNode interval = parse("foo [a...b)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void unbounded() {

		final IntervalNode interval = parse("foo (...)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_OPEN_BRACKET));
		assertThat(interval.getLeftBound(), nullValue());
		assertThat(interval.getRightBound(), nullValue());
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void leftBbounded() {

		final IntervalNode interval = parse("foo (a ...)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_OPEN_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), nullValue());
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void rightBounded() {

		final IntervalNode interval = parse("foo (... b]");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_OPEN_BRACKET));
		assertThat(interval.getLeftBound(), nullValue());
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_CLOSED_BRACKET));
	}

	@Test
	public void multiline() {

		final IntervalNode interval = parse(
				"foo [",
				"a ~~ left bound",
				"~~ ellipsis ~~ ...",
				"b ~~ right bound",
				"]");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_CLOSED_BRACKET));
	}

	@Test
	public void syntaxErrorAfterEllipsis() {
		expectError("syntax_error");

		final IntervalNode interval = parse(
				"foo [",
				"a ~~ left bound",
				"~~ ellipsis ~~ ... !!!",
				"b ~~ right bound",
				")");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void syntaxErrorAfterRightBound() {
		expectError("syntax_error");

		final IntervalNode interval = parse(
				"foo [",
				"a ~~ left bound",
				"~~ ellipsis ~~ ...",
				"b ~~ right bound",
				"!!!)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), isName("b"));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	@Test
	public void syntaxErrorInsteadOfRightBound() {
		expectError("syntax_error");
		expectError("syntax_error");

		final IntervalNode interval = parse(
				"foo [",
				"a ... !!!",
				"!!! ~~ erroneous right bound",
				"!!! ~~ second error ~~)");

		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(interval.getLeftBound(), isName("a"));
		assertThat(interval.getRightBound(), nullValue());
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
	}

	private IntervalNode parse(String... lines) {

		final PhraseNode phrase =
				to(PhraseNode.class, parseLines(expression(), lines));
		final IntervalNode interval =
				singlePhrasePart(IntervalNode.class, phrase);

		assertThat(signType(interval.getEllipsis()), is(HORIZONTAL_ELLIPSIS));

		return interval;
	}

}
