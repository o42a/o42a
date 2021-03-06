/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class SyntaxErrorTest extends GrammarTestCase {

	@Test
	public void unknownChar() {
		expectError("syntax_error");

		final ParenthesesNode result = parse(ParenthesesNode.class, "(\u2055)");
		final SentenceNode[] content = result.getContent();

		assertThat(content.length, is(0));
	}

	@Test
	public void unknownCharSequence() {
		expectError("syntax_error");

		final ParenthesesNode result =
				parse(ParenthesesNode.class, "(\u2055\u2055)");
		final SentenceNode[] content = result.getContent();

		assertThat(content.length, is(0));
	}

	@Test
	public void commentBetweenUnknown() {
		expectError("syntax_error");
		expectError("syntax_error");

		final ParenthesesNode result =
				parse(ParenthesesNode.class, "(\u2055 ~~ comment ~~ \u2055)");
		final SentenceNode[] content = result.getContent();

		assertThat(content.length, is(0));
	}

	@Test
	public void statementAfterUnknown() {
		expectError("syntax_error");

		final ParenthesesNode result =
				parse(ParenthesesNode.class, "(\u2055 foo)");

		assertThat(singleStatement(RefNode.class, result), isName("foo"));
	}

	@Test
	public void statementsMixedWithUnknown() {
		expectError("syntax_error");

		final ParenthesesNode result =
				parse(ParenthesesNode.class, "(foo,\u2055 bar)");

		assertThat(statement(RefNode.class, result, 0, 2), isName("foo"));
		assertThat(statement(RefNode.class, result, 1, 2), isName("bar"));
	}

	private <T> T parse(Class<? extends T> type, String text) {
		return to(type, parse(DECLARATIVE.statement(), text));
	}

}
