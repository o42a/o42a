/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.BodyRefNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.SelfAssignmentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class StatementTest extends GrammarTestCase {

	@Test
	public void selfAssignment() {

		final SelfAssignmentNode result =
				parse(SelfAssignmentNode.class, "= foo");

		assertThat(result.getPrefix(), hasRange(0, 1));
		assertThat(result.getValue(), hasRange(2, 5));
		assertThat(result.getValue(), isName("foo"));
	}

	@Test
	public void clauseDeclarator() {
		parse(ClauseDeclaratorNode.class, "<*> foo");
		parse(ClauseDeclaratorNode.class, "<*> foo()");
		parse(ClauseDeclaratorNode.class, "<*> foo = bar");
		parse(ClauseDeclaratorNode.class, "<*> (foo)");
		parse(ClauseDeclaratorNode.class, "<*> {foo}");
	}

	@Test
	public void parentheses() {
		parse(ParenthesesNode.class, "(foo)");
		parse(MemberRefNode.class, "(foo): bar");
		parse(PhraseNode.class, "(foo) bar");
		parse(PhraseNode.class, "(foo)\n_(bar)");
		parse(BinaryNode.class, "(foo) + bar");
		parse(BodyRefNode.class, "(foo)`");
		to(
				AssignmentNode.class,
				parse(IMPERATIVE.statement(), "(foo) = bar"));
	}

	@Test
	public void invalidDeclarator() {
		expectError("syntax_error");
		parse(DeclaratorNode.class, "A := boo () B := bar ()");
	}

	@Test
	public void invalidDeclarator2() {
		expectError("syntax_error");
		parse(DeclaratorNode.class, "A := boo: 42");
	}

	@Test
	public void initializer() {
		parse(DeclaratorNode.class, "A := boo () B = bar ()");
	}

	@Test
	public void validDeclarator() {
		parse(DeclaratorNode.class, "A := boo (`bar) baz");
		parse(DeclaratorNode.class, "A := boo (`bar) [baz]");
		parse(DeclaratorNode.class, "A := boo (`integer) 42");
		parse(DeclaratorNode.class, "A := boo` [42]");
		parse(DeclaratorNode.class, "A := boo` 42");
		parse(DeclaratorNode.class, "A := boo:: 42");
		parse(DeclaratorNode.class, "A := boo_ 42");
	}

	private <T> T parse(Class<? extends T> nodeType, String text) {
		return to(
				nodeType,
				parse(DECLARATIVE.statement(), text));
	}

}
