/*
    Parser Tests
    Copyright (C) 2012 Ruslan Lopatin

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.test.grammar.clause.ClauseDeclaratorTest.checkNothingReused;
import static org.o42a.ast.test.grammar.clause.ClauseDeclaratorTest.checkParentheses;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
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
	public void adapter() {

		final ClauseDeclaratorNode result = parse("<@foo> bar");

		assertThat(result.requiresContinuation(), is(false));
		assertThat(to(DeclarableAdapterNode.class, result.getClauseId())
		.getMember(), isName("foo"));
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

		final ClauseDeclaratorNode result = parse("<foo + bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.ADD));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void subtract() {

		final ClauseDeclaratorNode result = parse("<foo - bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.SUBTRACT));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void multiply() {

		final ClauseDeclaratorNode result = parse("<foo * bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.MULTIPLY));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void divide() {

		final ClauseDeclaratorNode result = parse("<foo / bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.DIVIDE));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void compare() {

		final ClauseDeclaratorNode result = parse("<foo <=> bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.COMPARE));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void equal() {

		final ClauseDeclaratorNode result = parse("<foo == bar> baz");
		final BinaryNode binary = to(BinaryNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(binary.getOperator(), is(BinaryOperator.EQUAL));
		assertThat(binary.getLeftOperand(), isName("foo"));
		assertThat(binary.getRightOperand(), isName("bar"));
		assertThat(result.getContent(), isName("baz"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	@Test
	public void phrase() {

		final ClauseDeclaratorNode result = parse("<*[foo]> bar");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseId());

		assertThat(result.requiresContinuation(), is(false));
		assertThat(
				to(ScopeRefNode.class, phrase.getPrefix()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(singleClause(BracketsNode.class, phrase)
		.getArguments()[0].getValue(), isName("foo"));
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
	public void rowPhrase() {

		final ClauseDeclaratorNode result = parse("<*[[foo]]> bar");
		final PhraseNode phrase = to(PhraseNode.class, result.getClauseId());

		assertFalse(result.requiresContinuation());
		assertEquals(
				ScopeType.IMPLIED,
				to(ScopeRefNode.class, phrase.getPrefix()).getType());

		final BracketsNode key = singleClause(BracketsNode.class, phrase);

		assertThat(key.getArguments().length, is(1));

		final BracketsNode row =
				to(BracketsNode.class, key.getArguments()[0].getValue());

		assertThat(row.getArguments().length, is(1));

		assertThat(row.getArguments()[0].getValue(), isName("foo"));
		assertThat(result.getContent(), isName("bar"));
		checkNothingReused(result);
		checkParentheses(result);
	}

	private ClauseDeclaratorNode parse(String text) {
		return parse(DECLARATIVE.clauseDeclarator(), text);
	}

}
