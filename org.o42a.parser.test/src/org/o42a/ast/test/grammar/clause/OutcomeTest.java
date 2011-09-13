/*
    Parser Tests
    Copyright (C) 2011 Ruslan Lopatin

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class OutcomeTest extends  GrammarTestCase {

	@Test
	public void outcome() {

		final ClauseDeclaratorNode result = parse("<Foo = bar> Val");

		assertName("foo", result.getClauseKey());
		assertName("bar", result.getOutcome().getValue());
	}

	@Test
	public void outcomeAndReuse() {

		final ClauseDeclaratorNode result = parse("<Foo = bar | baz> Val");

		assertName("foo", result.getClauseKey());
		assertName("bar", result.getOutcome().getValue());
		assertThat(result.getReused().length, is(1));
		assertName("baz", result.getReused()[0].getClause());
	}

	@Test
	public void outcomeAndContinuation() {

		final ClauseDeclaratorNode result = parse("<Foo = bar...> Val");

		assertName("foo", result.getClauseKey());
		assertName("bar", result.getOutcome().getValue());
		assertNotNull(result.getContinuation());
	}

	@Test
	public void outcomeWithoutValue() {
		expectError("missing_outcome_value");

		final ClauseDeclaratorNode result = parse("<Foo = | bar...> Val");

		assertName("foo", result.getClauseKey());
		assertNotNull(result.getOutcome());
		assertNull(result.getOutcome().getValue());
		assertThat(result.getReused().length, is(1));
		assertName("bar", result.getReused()[0].getClause());
		assertNotNull(result.getContinuation());
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
