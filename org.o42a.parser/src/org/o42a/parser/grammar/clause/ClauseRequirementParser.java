/*
    Parser
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.clause;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.clause.ClauseDeclaratorNode.Requirement;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.string.Characters;


final class ClauseRequirementParser
		implements Parser<SignNode<ClauseDeclaratorNode.Requirement>> {

	static final ClauseRequirementParser CLAUSE_REQUIREMENT =
			new ClauseRequirementParser();

	private ClauseRequirementParser() {
	}

	@Override
	public SignNode<Requirement> parse(ParserContext context) {

		final int next = context.next();
		final SourcePosition start;
		final ClauseDeclaratorNode.Requirement requirement;

		switch (next) {
		case '!':
			start = context.current().fix();
			context.acceptAll();
			requirement = ClauseDeclaratorNode.Requirement.TERMINATE;
			break;
		case Characters.HORIZONTAL_ELLIPSIS:
			start = context.current().fix();
			context.acceptAll();
			requirement = ClauseDeclaratorNode.Requirement.CONTINUE;
			break;
		case '.':
			start = context.current().fix();
			if (context.next() != '.') {
				return null;
			}
			if (context.next() != '.') {
				return null;
			}
			context.acceptAll();
			requirement = ClauseDeclaratorNode.Requirement.CONTINUE;
			break;
		default:
			return null;
		}

		return context.acceptComments(
				true,
				new SignNode<>(start, context.current().fix(), requirement));
	}

}
