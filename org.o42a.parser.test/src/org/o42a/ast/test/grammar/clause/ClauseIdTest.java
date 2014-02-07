/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.clause;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.phrase.IntervalBracket.*;
import static org.o42a.ast.test.grammar.clause.ClauseDeclaratorTest.checkNothingReused;
import static org.o42a.ast.test.grammar.clause.ClauseDeclaratorTest.checkParentheses;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ClauseIdTest extends GrammarTestCase {

	@Test
	public void name() {

		final ClauseDeclaratorNode result = parse("<foo> bar");

		assertFalse(result.requiresContinuation());
		assertThat(result.getClauseId(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void emptyBrackets() {

		final ClauseDeclaratorNode result = parse("<[]> bar");

		assertThat(result.requiresContinuation(), is(false));
		assertThat(to(BracketsNode.class, result.getClauseId())
		.getArguments().length, is(0));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void brackets() {

		final ClauseDeclaratorNode result = parse("<[foo]> bar");
		final ArgumentNode[] arguments =
				to(BracketsNode.class, result.getClauseId()).getArguments();

		assertThat(result.requiresContinuation(), is(false));
		assertThat(arguments.length, is(1));
		assertThat(arguments[0].isInitializer(), is(false));
		assertThat(arguments[0].getValue(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void initializer() {

		final ClauseDeclaratorNode result = parse("<[= foo]> bar");
		final ArgumentNode[] arguments =
				to(BracketsNode.class, result.getClauseId()).getArguments();

		assertThat(result.requiresContinuation(), is(false));
		assertThat(arguments.length, is(1));
		assertThat(arguments[0].isInitializer(), is(true));
		assertThat(arguments[0].getValue(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void emptyString() {

		final ClauseDeclaratorNode result = parse("<''> bar");

		assertThat(result.requiresContinuation(), is(false));
		assertThat(
				to(StringNode.class, result.getClauseId()).getText().length(),
				is(0));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void string() {

		final ClauseDeclaratorNode result = parse("<'foo'> bar");

		assertThat(result.requiresContinuation(), is(false));
		assertThat(
				to(StringNode.class, result.getClauseId()).getText(),
				is("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void emptyBraces() {

		final ClauseDeclaratorNode result = parse("<{}> bar");

		assertThat(result.requiresContinuation(), is(false));
		assertThat(to(BracesNode.class, result.getClauseId())
		.getContent().length, is(0));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void emptyRow() {

		final ClauseDeclaratorNode result = parse("<[[]]> bar");
		final ArgumentNode[] arguments =
				to(BracketsNode.class, result.getClauseId()).getArguments();

		assertThat(result.requiresContinuation(), is(false));
		assertThat(arguments.length, is(1));

		final ArgumentNode[] items =
				to(BracketsNode.class, arguments[0].getValue()).getArguments();

		assertThat(items.length, is(0));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void row() {

		final ClauseDeclaratorNode result = parse("<[[foo]]> bar");
		final ArgumentNode[] arguments =
				to(BracketsNode.class, result.getClauseId()).getArguments();

		assertThat(result.requiresContinuation(), is(false));
		assertThat(arguments.length, is(1));

		final ArgumentNode[] items =
				to(BracketsNode.class, arguments[0].getValue()).getArguments();

		assertThat(items.length, is(1));
		assertThat(items[0].getValue(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void braces() {

		final ClauseDeclaratorNode result = parse("<{foo}> bar");
		final BracesNode braces = to(BracesNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(singleStatement(MemberRefNode.class, braces), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void plus() {

		final ClauseDeclaratorNode result = parse("<+foo> bar");
		final UnaryNode unary = to(UnaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(unary.getOperator(), is(UnaryOperator.PLUS));
		assertThat(unary.getOperand(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void minus() {

		final ClauseDeclaratorNode result = parse("<- foo> bar");
		final UnaryNode unary = to(UnaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(unary.getOperator(), is(UnaryOperator.MINUS));
		assertThat(unary.getOperand(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void add() {

		final ClauseDeclaratorNode result = parse("<foo + *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.ADD));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void subtract() {

		final ClauseDeclaratorNode result = parse("<foo - *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.SUBTRACT));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void multiply() {

		final ClauseDeclaratorNode result = parse("<foo * *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.MULTIPLY));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void divide() {

		final ClauseDeclaratorNode result = parse("<foo / *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.DIVIDE));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void compare() {

		final ClauseDeclaratorNode result = parse("<foo <=> *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.COMPARE));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void equal() {

		final ClauseDeclaratorNode result = parse("<foo == *> bar");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.EQUAL));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, binary.getRightOperand()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void assign() {

		final ClauseDeclaratorNode result = parse("<foo <- *> bar");
		final AssignmentNode assignment =
				to(AssignmentNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(assignment.getDestination(), isName("foo"));
		assertThat(
				to(ScopeRefNode.class, assignment.getValue()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void implied() {

		final ClauseDeclaratorNode result = parse("<*> foo");

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, result.getClauseId()).getType());
		assertThat(result.getContent(), isName("foo"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void openInterval() {

		final ClauseDeclaratorNode result = parse("<(...)> bar");
		final IntervalNode interval =
				to(IntervalNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertThat(result.getContent(), isName("bar"));
		assertThat(signType(interval.getLeftBracket()), is(LEFT_OPEN_BRACKET));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_OPEN_BRACKET));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void closedInterval() {

		final ClauseDeclaratorNode result = parse("<[foo...*]> bar");
		final IntervalNode interval =
				to(IntervalNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertThat(result.getContent(), isName("bar"));
		assertThat(
				signType(interval.getLeftBracket()),
				is(LEFT_CLOSED_BRACKET));
		assertThat(
				signType(interval.getRightBracket()),
				is(RIGHT_CLOSED_BRACKET));
		checkNothingReused(result);
		checkParentheses(result);
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
