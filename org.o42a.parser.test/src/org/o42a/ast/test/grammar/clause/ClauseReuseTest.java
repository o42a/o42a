/*
    Parser Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
		assertName("foo", result.getClauseKey());
		assertName("val", result.getContent());

		final ReusedClauseNode[] reused = result.getReused();

		assertEquals(2, reused.length);

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[0].getSeparator().getType());
		assertName("bar", reused[0].getClause());
		assertNull(reused[0].getReuseContents());

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[1].getSeparator().getType());
		assertName("baz", reused[1].getClause());
		assertNull(reused[1].getReuseContents());

		checkParentheses(result);
	}

	@Test
	public void reuseAndContinuation() {

		final ClauseDeclaratorNode result = parse("<foo | bar ...> ()");

		assertRange(11, 14, result.getRequirement());

		final ReusedClauseNode[] reused = result.getReused();

		assertThat(reused.length, is(1));
		assertNull(reused[0].getReuseContents());

		checkParentheses(result);
	}

	@Test
	public void reuseContents() {

		final ClauseDeclaratorNode result = parse("<foo | bar* | baz> val");

		assertFalse(result.requiresContinuation());
		assertName("foo", result.getClauseKey());
		assertName("val", result.getContent());

		final ReusedClauseNode[] reused = result.getReused();

		assertEquals(2, reused.length);

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[0].getSeparator().getType());
		assertName("bar", reused[0].getClause());
		assertThat(
				reused[0].getReuseContents().getType(),
				is(ReusedClauseNode.ReuseContents.ASTERISK));

		assertEquals(
				ReusedClauseNode.Separator.OR,
				reused[1].getSeparator().getType());
		assertName("baz", reused[1].getClause());
		assertNull(reused[1].getReuseContents());

		checkParentheses(result);
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
