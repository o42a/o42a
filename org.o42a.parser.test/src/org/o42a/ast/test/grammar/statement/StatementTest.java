/*
    Parser Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.statement.ClauseDeclaratorNode;
import org.o42a.ast.statement.SelfAssignmentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class StatementTest extends GrammarTestCase {

	@Test
	public void selfAssignment() {

		final SelfAssignmentNode result =
			parse(SelfAssignmentNode.class, "= foo");

		assertRange(0, 1, result.getPrefix());
		assertRange(2, 5, result.getValue());
		assertName("foo", result.getValue());
	}

	@Test
	public void clauseDeclarator() {
		parse(ClauseDeclaratorNode.class, "<*> foo");
		parse(ClauseDeclaratorNode.class, "<*> foo()");
		parse(ClauseDeclaratorNode.class, "<*> foo = bar");
		parse(ClauseDeclaratorNode.class, "<*> (foo)");
		parse(ClauseDeclaratorNode.class, "<*> {foo}");
	}

	private <T> T parse(Class<? extends T> nodeType, String text) {
		return to(
				nodeType,
				super.parse(DECLARATIVE.statement(), text));
	}

}
