/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.clause;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.o42a.ast.test.grammar.clause.ClauseDeclaratorTest.checkParentheses;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.clause.ReusedClauseNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ClauseReuseTest extends GrammarTestCase {

	@Test
	public void reuseClause() {

		final ClauseDeclaratorNode result = parse("<foo|bar|baz> val");

		assertFalse(result.requiresContinuation());
		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getContent(), isName("val"));

		final ReusedClauseNode[] reused = result.getReused();

		assertEquals(2, reused.length);

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[0].getSeparator().getType());
		assertThat(reused[0].getClause(), isName("bar"));
		assertNull(reused[0].getReuseContents());

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[1].getSeparator().getType());
		assertThat(reused[1].getClause(), isName("baz"));
		assertNull(reused[1].getReuseContents());

		checkParentheses(result);
	}

	@Test
	public void reuseAndContinuation() {

		final ClauseDeclaratorNode result = parse("<foo | bar ...> ()");

		assertThat(result.getRequirement(), hasRange(11, 14));

		final ReusedClauseNode[] reused = result.getReused();

		assertThat(reused.length, is(1));
		assertNull(reused[0].getReuseContents());

		checkParentheses(result);
	}

	@Test
	public void reuseContents() {

		final ClauseDeclaratorNode result = parse("<foo | bar* | baz> val");

		assertFalse(result.requiresContinuation());
		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getContent(), isName("val"));

		final ReusedClauseNode[] reused = result.getReused();

		assertEquals(2, reused.length);

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[0].getSeparator().getType());
		assertThat(reused[0].getClause(), isName("bar"));
		assertThat(
				reused[0].getReuseContents().getType(),
				is(ReusedClauseNode.ReuseContents.ASTERISK));

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[1].getSeparator().getType());
		assertThat(reused[1].getClause(), isName("baz"));
		assertNull(reused[1].getReuseContents());

		checkParentheses(result);
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
