/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.ast.atom.HorizontalEllipsis;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;
import org.o42a.ast.statement.EllipsisNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.parser.Grammar;


public class EllipsisTest extends GrammarTestCase {

	@Test
	public void onlyEllipsis() {

		final SentenceNode sentence = parse("...");
		final EllipsisNode ellipsis =
				singleStatement(EllipsisNode.class, sentence);

		assertThat(sentence.getMark(), nullValue());
		assertThat(ellipsis, hasRange(0, 3));
		assertThat(ellipsis.getTarget(), nullValue());
		assertThat(
				ellipsis.getMark().getType(),
				is(HorizontalEllipsis.HORIZONTAL_ELLIPSIS));
	}

	@Test
	public void onlyHorizontalEllipsis() {

		final SentenceNode sentence = parse("\u2026");
		final EllipsisNode ellipsis =
				singleStatement(EllipsisNode.class, sentence);

		assertThat(sentence.getMark(), nullValue());
		assertThat(ellipsis, hasRange(0, 1));
		assertThat(ellipsis.getTarget(), nullValue());
		assertThat(
				ellipsis.getMark().getType(),
				is(HorizontalEllipsis.HORIZONTAL_ELLIPSIS));
	}

	@Test
	public void onlyTargetedEllipsis() {

		final SentenceNode sentence = parse("... bar");
		final EllipsisNode ellipsis =
				singleStatement(EllipsisNode.class, sentence);

		assertThat(sentence.getMark(), nullValue());
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void onlyTargetedHorizontalEllipsis() {

		final SentenceNode sentence = parse("\u2026 bar");
		final EllipsisNode ellipsis =
				singleStatement(EllipsisNode.class, sentence);

		assertThat(sentence.getMark(), nullValue());
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void untargetedEllipsis() {

		final SentenceNode sentence = parse("foo ...");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark(), nullValue());
		assertThat(statement, isName("foo"));
		assertThat(ellipsis, hasRange(4, 7));
		assertThat(ellipsis.getTarget(), nullValue());
	}

	@Test
	public void untargetedHorizontalEllipsis() {

		final SentenceNode sentence = parse("foo \u2026");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark(), nullValue());
		assertThat(statement, isName("foo"));
		assertThat(ellipsis, hasRange(4, 5));
		assertThat(ellipsis.getTarget(), nullValue());
	}

	@Test
	public void targetedEllipsis() {

		final SentenceNode sentence = parse("foo ... bar");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark(), nullValue());
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void targetedHorizontalEllipsis() {

		final SentenceNode sentence = parse("foo \u2026 bar");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark(), nullValue());
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void lastInProposition() {

		final SentenceNode sentence = parse("foo ....");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.DECLARATION));
		assertThat(statement, isName("foo"));
		assertThat(ellipsis.getTarget(), nullValue());
	}

	@Test
	public void horizontalEllipsisLastInProposition() {

		final SentenceNode sentence = parse("foo \u2026.");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.DECLARATION));
		assertThat(statement, isName("foo"));
		assertThat(ellipsis.getTarget(), nullValue());
	}

	@Test
	public void repeat() {

		final SentenceNode sentence = parse("foo ... bar.");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.DECLARATION));
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void repeatByHorizontalEllipsis() {

		final SentenceNode sentence = parse("foo \u2026 bar.");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.DECLARATION));
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void stop() {

		final SentenceNode sentence = parse("foo ... bar!");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.EXCLAMATION));
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	@Test
	public void stopByHorizontalEllipsis() {

		final SentenceNode sentence = parse("foo \u2026 bar!");
		final MemberRefNode statement =
				statement(MemberRefNode.class, sentence, 0, 2);
		final EllipsisNode ellipsis =
				statement(EllipsisNode.class, sentence, 1, 2);

		assertThat(sentence.getMark().getType(), is(SentenceType.EXCLAMATION));
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	private SentenceNode parse(String text) {
		return parse(Grammar.IMPERATIVE.sentence(), text);
	}

}
