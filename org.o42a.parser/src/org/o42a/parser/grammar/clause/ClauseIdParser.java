/*
    Parser
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.*;
import static org.o42a.parser.grammar.clause.UnaryClauseIdParser.UNARY_CLAUSE_ID;
import static org.o42a.util.string.Characters.MINUS_SIGN;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class ClauseIdParser implements Parser<ClauseIdNode> {

	public static final ClauseIdParser CLAUSE_ID = new ClauseIdParser();

	private ClauseIdParser() {
	}

	@Override
	public ClauseIdNode parse(ParserContext context) {
		switch (context.next()) {
		case '[':

			final IntervalNode leftClosedInterval = context.parse(interval());

			if (leftClosedInterval != null) {
				return leftClosedInterval;
			}

			return context.parse(brackets());
		case '(':
			return context.parse(interval());
		case '{':
			return context.parse(braces());
		case '+':
		case '-':
		case MINUS_SIGN:
			return context.parse(UNARY_CLAUSE_ID);
		case '\'':
			return context.parse(string());
		}

		final RefNode ref = context.parse(ref());

		if (ref == null) {
			return null;
		}

		if (context.pendingOrNext() == '<') {

			final AssignmentNode assignment =
					context.parse(new AssignmentClauseIdParser(ref));

			if (assignment != null) {
				return assignment;
			}
		}

		final BinaryNode binary = context.parse(new BinaryClauseIdParser(ref));

		if (binary != null) {
			return binary;
		}

		return ref.toClauseId();
	}

}
