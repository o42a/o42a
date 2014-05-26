/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.NamedBlockNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BlockTest extends GrammarTestCase {

	@Test
	public void emptyParentheses() {

		final ParenthesesNode result = to(
				ParenthesesNode.class,
				parse(DECLARATIVE.statement(), "(  )"));

		assertThat(result.getContent().length, is(0));
	}

	@Test
	public void emptyBraces() {

		final BracesNode result = to(
				BracesNode.class,
				parse(DECLARATIVE.statement(), "{  }"));

		assertThat(result.getContent().length, is(0));
	}

	@Test
	public void declaratorWithinParentheses() {

		final ParenthesesNode result = to(
				ParenthesesNode.class,
				parse(DECLARATIVE.statement(), "(foo = bar)"));
		final DeclaratorNode declarator =
				singleStatement(DeclaratorNode.class, result);

		assertThat(
				declarator.getTarget(),
				is(DeclarationTarget.OVERRIDE_VALUE));
		assertThat(declarator.getDeclarable(), isName("foo"));
		assertThat(declarator.getDefinition(), isName("bar"));
	}

	@Test
	public void emptyBracesWithinParentheses() {

		final ParenthesesNode result = to(
				ParenthesesNode.class,
				parse(DECLARATIVE.statement(), "({  })"));
		final BracesNode braces =
				singleStatement(BracesNode.class, result);

		assertEquals(0, braces.getContent().length);
	}

	@Test
	public void assignmentWithinBraces() {

		final BracesNode result = to(
				BracesNode.class,
				parse(IMPERATIVE.statement(), "{foo << bar}"));
		final AssignmentNode assignment =
				singleStatement(AssignmentNode.class, result);

		assertThat(assignment.getDestination(), isName("foo"));
		assertThat(assignment.getValue(), isName("bar"));
	}

	@Test
	public void assignmentWithinParenthesesInsideBraces() {

		final BracesNode result = to(
				BracesNode.class,
				parse(IMPERATIVE.statement(), "{(foo << bar)}"));
		final ParenthesesNode parentheses =
				singleStatement(ParenthesesNode.class, result);
		final AssignmentNode assignment =
				singleStatement(AssignmentNode.class, parentheses);

		assertThat(assignment.getDestination(), isName("foo"));
		assertThat(assignment.getValue(), isName("bar"));
	}

	@Test
	public void namedBlock() {

		final NamedBlockNode result = to(
				NamedBlockNode.class,
				parse(IMPERATIVE.statement(), "foo: {bar}"));

		assertThat(canonicalName(result.getName()), is("foo"));
		assertThat(
				result.getSeparator().getType(),
				is(NamedBlockNode.Separator.COLON));
		assertThat(
				singleStatement(
						MemberRefNode.class,
						result.getBlock()),
				isName("bar"));
	}

	@Test
	public void emptyNamedBlock() {

		final NamedBlockNode result = to(
				NamedBlockNode.class,
				parse(IMPERATIVE.statement(), "foo: {}"));

		assertThat(canonicalName(result.getName()), is("foo"));
		assertThat(
				result.getSeparator().getType(),
				is(NamedBlockNode.Separator.COLON));
		assertThat(result.getBlock().getContent().length, is(0));
	}

}
