/*
    Parser Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
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
				is(EllipsisNode.Mark.ELLIPSIS));
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
				is(EllipsisNode.Mark.ELLIPSIS));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.PROPOSITION));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.PROPOSITION));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.PROPOSITION));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.PROPOSITION));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.CLAIM));
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

		assertThat(sentence.getMark().getType(), is(SentenceType.CLAIM));
		assertThat(statement, isName("foo"));
		assertThat(canonicalName(ellipsis.getTarget()), is("bar"));
	}

	private SentenceNode parse(String text) {
		return parse(Grammar.IMPERATIVE.sentence(), text);
	}

}
