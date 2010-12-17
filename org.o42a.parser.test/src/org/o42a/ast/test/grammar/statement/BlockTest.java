/*
    Parser Tests
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.junit.Assert.assertEquals;
import static org.o42a.parser.Grammar.DECLARATIVE;
import static org.o42a.parser.Grammar.IMPERATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BlockTest extends GrammarTestCase {

	@Test
	public void declaratorWithinParentheses() {

		final ParenthesesNode result = to(
				ParenthesesNode.class,
				parse(DECLARATIVE.statement(), "(foo = bar)"));
		final DeclaratorNode declarator =
			singleStatement(DeclaratorNode.class, result);

		assertEquals(DeclarationTarget.OVERRIDE_VALUE, declarator.getTarget());
		assertName("foo", declarator.getDeclarable());
		assertName("bar", declarator.getDefinition());
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

		assertEquals("foo", result.getName().getName());
		assertEquals(
				NamedBlockNode.Separator.COLON,
				result.getSeparator().getType());
		assertName(
				"bar",
				singleStatement(MemberRefNode.class, result.getBlock()));
	}

}
