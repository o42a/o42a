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
package org.o42a.ast.test.grammar.clause;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.SelfAssignmentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ClauseDeclaratorTest extends GrammarTestCase {

	@Test
	public void nameKey() {

		final ClauseDeclaratorNode result = parse("<foo> bar");

		assertFalse(result.requiresContinuation());
		assertName("foo", result.getClauseId());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void adapterId() {

		final ClauseDeclaratorNode result = parse("<@foo> bar");

		assertFalse(result.requiresContinuation());
		assertName(
				"foo",
				to(DeclarableAdapterNode.class, result.getClauseId())
				.getMember());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void phraseId() {

		final ClauseDeclaratorNode result = parse("<*[foo]> bar");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, phrase.getPrefix()).getType());
		assertName(
				"foo",
				singleClause(BracketsNode.class, phrase)
				.getArguments()[0].getValue());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void impliedId() {

		final ClauseDeclaratorNode result = parse("<*> foo");

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, result.getClauseId()).getType());
		assertName("foo", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void noContent() {

		final ClauseDeclaratorNode result = parse("<*'foo'>");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertEquals(
				"foo",
				singleClause(TextNode.class, phrase).getText());
		assertNull(result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void rowId() {

		final ClauseDeclaratorNode result = parse("<*[[foo]]> bar");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, phrase.getPrefix()).getType());

		final BracketsNode key = singleClause(BracketsNode.class, phrase);

		assertThat(key.getArguments().length, is(1));

		final BracketsNode row =
				to(BracketsNode.class, key.getArguments()[0].getValue());

		assertThat(row.getArguments().length, is(1));

		assertName("foo", row.getArguments()[0].getValue());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void content() {
		to(ParenthesesNode.class, parse("<*> (foo)").getContent());
		to(BracesNode.class, parse("<*> {foo}").getContent());
		to(DeclaratorNode.class, parse("<*> foo = bar").getContent());
		to(SelfAssignmentNode.class, parse("<*> = foo").getContent());
		to(PhraseNode.class, parse("<*> foo()").getContent());
	}

	@Test
	public void continuation() {

		final ClauseDeclaratorNode result = parse("<foo...> ()");

		assertRange(4, 7, result.getRequirement());
		assertTrue(result.requiresContinuation());
		assertThat(result.getReused().length, is(0));
	}

	@Test
	public void terminator() {

		final ClauseDeclaratorNode result = parse("<foo!> ()");

		assertRange(4, 5, result.getRequirement());
		assertTrue(result.isTerminator());
		assertThat(result.getReused().length, is(0));
	}

	private void assertNothingReused(ClauseDeclaratorNode declarator) {
		assertEquals(0, declarator.getReused().length);
	}

	static void checkParentheses(ClauseDeclaratorNode declarator) {
		assertEquals(
				ClauseDeclaratorNode.Parenthesis.OPENING,
				declarator.getOpening().getType());
		assertEquals(
				ClauseDeclaratorNode.Parenthesis.CLOSING,
				declarator.getClosing().getType());
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
