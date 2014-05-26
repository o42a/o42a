/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class DeclarativeAssignmentTest extends GrammarTestCase {

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
	public void localAssignment() {

		final AssignmentNode assignment = parse("A $<< b + $");
		final LocalNode local = assignment.getDestination().toLocal();

		assertThat(local.getExpression(), isName("a"));
		assertThat(local.getName(), nullValue());
		assertThat(
				assignment.getOperator().getType(),
				is(AssignmentOperator.ASSIGN));

		final BinaryNode value = to(BinaryNode.class, assignment.getValue());

		assertThat(value.getLeftOperand(), isName("b"));
		assertThat(
				to(ScopeRefNode.class, value.getRightOperand()).getType(),
				is(ScopeType.LOCAL));
	}

	@Test
	public void localBinding() {

		final AssignmentNode assignment = parse("A $<- b + $");
		final LocalNode local = assignment.getDestination().toLocal();

		assertThat(local.getExpression(), isName("a"));
		assertThat(local.getName(), nullValue());
		assertThat(
				assignment.getOperator().getType(),
				is(AssignmentOperator.BIND));

		final BinaryNode value = to(BinaryNode.class, assignment.getValue());

		assertThat(value.getLeftOperand(), isName("b"));
		assertThat(
				to(ScopeRefNode.class, value.getRightOperand()).getType(),
				is(ScopeType.LOCAL));
	}

	@Test
	public void namedLocalAssignment() {

		final AssignmentNode assignment = parse("A $ tmp << b + tmp");
		final LocalNode local = assignment.getDestination().toLocal();

		assertThat(local.getExpression(), isName("a"));
		assertThat(canonicalName(local.getName()), is("tmp"));
		assertThat(
				assignment.getOperator().getType(),
				is(AssignmentOperator.ASSIGN));

		final BinaryNode value = to(BinaryNode.class, assignment.getValue());

		assertThat(value.getLeftOperand(), isName("b"));
		assertThat(value.getRightOperand(), isName("tmp"));
	}

	private AssignmentNode parse(String text) {
		return to(AssignmentNode.class, parse(DECLARATIVE.statement(), text));
	}

}
