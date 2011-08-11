/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import static org.junit.Assert.*;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ClauseDeclaratorTest extends GrammarTestCase {

	@Test
	public void nameKey() {

		final ClauseDeclaratorNode result = parse("<foo> bar");

		assertFalse(result.requiresContinuation());
		assertName("foo", result.getClauseKey());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void adapterKey() {

		final ClauseDeclaratorNode result = parse("<@foo> bar");

		assertFalse(result.requiresContinuation());
		assertName(
				"foo",
				to(DeclarableAdapterNode.class, result.getClauseKey())
				.getMember());
		assertName("bar", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void phraseKey() {

		final ClauseDeclaratorNode result = parse("<*[foo]> bar");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseKey());

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
	public void impliedKey() {

		final ClauseDeclaratorNode result = parse("<*> foo");

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, result.getClauseKey()).getType());
		assertName("foo", result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void noContent() {

		final ClauseDeclaratorNode result = parse("<*'foo'>");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseKey());

		assertFalse(result.requiresContinuation());
		assertEquals(
				"foo",
				singleClause(TextNode.class, phrase).getText());
		assertNull(result.getContent());
		assertNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void reuseClause() {

		final ClauseDeclaratorNode result = parse("<foo|bar|baz> val");

		assertFalse(result.requiresContinuation());
		assertName("foo", result.getClauseKey());
		assertName("val", result.getContent());

		final ReusedClauseNode[] reused = result.getReused();

		assertEquals(2, reused.length);

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[0].getSeparator().getType());
		assertName("bar", reused[0].getClause());

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[1].getSeparator().getType());
		assertName("baz", reused[1].getClause());

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

		assertRange(4, 7, result.getContinuation());
		assertThat(result.getReused().length, is(0));
	}

	@Test
	public void reuseAndContinuation() {

		final ClauseDeclaratorNode result = parse("<foo | bar ...> ()");

		assertRange(11, 14, result.getContinuation());
		assertThat(result.getReused().length, is(1));
	}

	private void assertNothingReused(ClauseDeclaratorNode declarator) {
		assertEquals(0, declarator.getReused().length);
	}

	private void checkParentheses(ClauseDeclaratorNode declarator) {
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
