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
package org.o42a.ast.test.grammar.sentence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ContentTest extends GrammarTestCase {

	@Test
	public void twoSentences() {

		final SentenceNode[] result = parse("foo. bar.");

		assertThat(result.length, is(2));
		assertThat(singleStatement(MemberRefNode.class, result[0]), isName("foo"));
		assertThat(singleStatement(MemberRefNode.class, result[1]), isName("bar"));
	}

	@Test
	public void contentWithCalls() {

		final SentenceNode[] result = parse("a(foo = bar). b(bar = foo).");

		assertThat(result.length, is(2));

		final PhraseNode call1 = singleStatement(PhraseNode.class, result[0]);

		assertThat(call1.getPrefix(), isName("a"));

		final DeclaratorNode decl1 = singleStatement(
				DeclaratorNode.class,
				singleClause(ParenthesesNode.class, call1));

		assertThat(decl1.getDeclarable(), isName("foo"));
		assertThat(decl1.getDefinition(), isName("bar"));

		final PhraseNode call2 = singleStatement(PhraseNode.class, result[1]);

		assertThat(call2.getPrefix(), isName("b"));

		final DeclaratorNode decl2 = singleStatement(
				DeclaratorNode.class,
				singleClause(ParenthesesNode.class, call2));

		assertThat(decl2.getDeclarable(), isName("bar"));
		assertThat(decl2.getDefinition(), isName("foo"));
	}

	@Test
	public void sentenceBreak() {

		final SentenceNode[] result = parse(
				"a()",
				"b()");

		assertThat(result.length, is(2));
		assertThat(result[0].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[0].getMark());
		assertThat(result[1].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[1].getMark());
	}

	@Test
	public void sentenceBreakByEmptyStatement() {
		expectError("empty_statement");

		final SentenceNode[] result = parse(
				"a",
				", b");

		assertThat(result.length, is(2));
		assertThat(result[0].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[0].getMark());
		assertThat(result[1].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[1].getMark());
	}

	@Test
	public void sentenceBreakByEmptyAlternative() {
		expectError("empty_alternative");

		final SentenceNode[] result = parse(
				"a",
				"; b");

		assertThat(result.length, is(2));
		assertThat(result[0].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[0].getMark());
		assertThat(result[1].getType(), is(SentenceType.PROPOSITION));
		assertNull(result[1].getMark());
	}

	public void sentenceBreakByNames() {

		final SentenceNode[] result = parse(
				"a",
				"b");

		assertThat(result.length, is(2));
		assertThat(singleStatement(MemberRefNode.class, result[0]), isName("a"));
		assertThat(singleStatement(MemberRefNode.class, result[1]), isName("b"));
	}

	private SentenceNode[] parse(String... lines) {
		return parseLines(DECLARATIVE.content(), lines);
	}

}
