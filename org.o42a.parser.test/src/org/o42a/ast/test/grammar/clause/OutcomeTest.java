/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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

		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getOutcome().getValue(), isName("bar"));
	}

	@Test
	public void outcomeAndReuse() {

		final ClauseDeclaratorNode result = parse("<Foo = bar | baz> Val");

		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getOutcome().getValue(), isName("bar"));
		assertThat(result.getReused().length, is(1));
		assertThat(result.getReused()[0].getClause(), isName("baz"));
	}

	@Test
	public void outcomeAndContinuation() {

		final ClauseDeclaratorNode result = parse("<Foo = bar...> Val");

		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getOutcome().getValue(), isName("bar"));
		assertNotNull(result.getRequirement());
	}

	@Test
	public void outcomeWithoutValue() {
		expectError("missing_outcome_value");

		final ClauseDeclaratorNode result = parse("<Foo = | bar...> Val");

		assertThat(result.getClauseId(), isName("foo"));
		assertNotNull(result.getOutcome());
		assertNull(result.getOutcome().getValue());
		assertThat(result.getReused().length, is(1));
		assertThat(result.getReused()[0].getClause(), isName("bar"));
		assertNotNull(result.getRequirement());
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
