/*
    Parser Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
		assertName("foo", declarator.getDeclarable());
		assertName("bar", declarator.getDefinition());
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
				parse(IMPERATIVE.statement(), "{foo = bar}"));
		final AssignmentNode assignment =
				singleStatement(AssignmentNode.class, result);

		assertName("foo", assignment.getDestination());
		assertName("bar", assignment.getValue());
	}

	@Test
	public void assignmentWithinParenthesesInsideBraces() {

		final BracesNode result = to(
				BracesNode.class,
				parse(IMPERATIVE.statement(), "{(foo = bar)}"));
		final ParenthesesNode parentheses =
				singleStatement(ParenthesesNode.class, result);
		final AssignmentNode assignment =
				singleStatement(AssignmentNode.class, parentheses);

		assertName("foo", assignment.getDestination());
		assertName("bar", assignment.getValue());
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
		assertName(
				"bar",
				singleStatement(MemberRefNode.class, result.getBlock()));
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
