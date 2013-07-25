/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class AssignmentTest extends GrammarTestCase {

	@Test
	public void assignment() {

		final AssignmentNode assignment = parse("foo = bar");

		assertThat(assignment, hasRange(0, 9));
		assertThat(assignment.getDestination(), isName("foo"));
		assertThat(
				assignment.getOperator().getType(),
				is(AssignmentOperator.ASSIGN));
		assertThat(assignment.getValue(), isName("bar"));
	}

	@Test
	public void binding() {

		final AssignmentNode assignment = parse("foo <- bar");

		assertThat(assignment, hasRange(0, 10));
		assertThat(assignment.getDestination(), isName("foo"));
		assertThat(
				assignment.getOperator().getType(),
				is(AssignmentOperator.BIND));
		assertThat(assignment.getValue(), isName("bar"));
	}

	@Test
	public void binaryExpressionValue() {

		final AssignmentNode assignment = parse("foo = bar + baz");

		assertThat(assignment.getDestination(), isName("foo"));

		final BinaryNode value =
				to(BinaryNode.class, assignment.getValue());

		assertEquals(BinaryOperator.ADD, value.getOperator());
		assertThat(value.getLeftOperand(), isName("bar"));
		assertThat(value.getRightOperand(), isName("baz"));
	}

	@Test
	public void binaryExpressionDestination() {

		final AssignmentNode assignment = parse("foo + bar = baz");
		final BinaryNode destination =
				to(BinaryNode.class, assignment.getDestination());

		assertEquals(BinaryOperator.ADD, destination.getOperator());
		assertThat(destination.getLeftOperand(), isName("foo"));
		assertThat(destination.getRightOperand(), isName("bar"));
		assertThat(assignment.getValue(), isName("baz"));
	}

	@Test
	public void localAssignment() {

		final AssignmentNode assignment = parse("A $= b + $");
		final LocalNode local = assignment.getDestination().toLocal();

		assertThat(local.getExpression(), isName("a"));
		assertThat(local.getName(), nullValue());

		final BinaryNode value = to(BinaryNode.class, assignment.getValue());

		assertThat(value.getLeftOperand(), isName("b"));
		assertThat(
				to(ScopeRefNode.class, value.getRightOperand()).getType(),
				is(ScopeType.LOCAL));
	}

	@Test
	public void namedLocalAssignment() {

		final AssignmentNode assignment = parse("A $ local = b + $local");
		final LocalNode local = assignment.getDestination().toLocal();

		assertThat(local.getExpression(), isName("a"));
		assertThat(local.getName().getName().toString(), is("local"));

		final BinaryNode value = to(BinaryNode.class, assignment.getValue());

		assertThat(value.getLeftOperand(), isName("b"));

		final MemberRefNode right =
				to(MemberRefNode.class, value.getRightOperand());

		assertThat(right, memberRefWithoutRetention());
		assertThat(
				to(ScopeRefNode.class, right.getOwner()).getType(),
				is(ScopeType.LOCAL));
		assertThat(right, hasName("local"));
	}

	private AssignmentNode parse(String text) {
		return to(AssignmentNode.class, parse(IMPERATIVE.statement(), text));
	}

}
