/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.clause;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.statement.ReturnNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ClauseDeclaratorTest extends GrammarTestCase {

	@Test
	public void noContent() {

		final ClauseDeclaratorNode result = parse("<'foo'>");
		final StringNode string = to(StringNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(string.getText(), is("foo"));
		assertThat(result.getContent(), nullValue());
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void content() {
		to(ParenthesesNode.class, parse("<*> (foo)").getContent());
		to(BracesNode.class, parse("<*> {foo}").getContent());
		to(DeclaratorNode.class, parse("<*> foo = bar").getContent());
		to(ReturnNode.class, parse("<*> = foo").getContent());
		to(PhraseNode.class, parse("<*> foo()").getContent());
	}

	@Test
	public void continuation() {

		final ClauseDeclaratorNode result = parse("<foo...> ()");

		assertThat(result.getRequirement(), hasRange(4, 7));
		assertTrue(result.requiresContinuation());
		assertThat(result.getReused().length, is(0));
	}

	@Test
	public void terminator() {

		final ClauseDeclaratorNode result = parse("<foo!> ()");

		assertThat(result.getRequirement(), hasRange(4, 5));
		assertThat(result.isTerminator(), is(true));
		assertThat(result.getReused().length, is(0));
	}

	static void checkNothingReused(ClauseDeclaratorNode declarator) {
		assertThat(declarator.getReused().length, is(0));
	}

	static void checkParentheses(ClauseDeclaratorNode declarator) {
		assertThat(
				declarator.getOpening().getType(),
				is(ClauseDeclaratorNode.Parenthesis.OPENING));
		assertThat(
				declarator.getClosing().getType(),
				is(ClauseDeclaratorNode.Parenthesis.CLOSING));
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
