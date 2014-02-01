/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.parser.grammar.clause.ClauseIdParser.CLAUSE_ID;
import static org.o42a.parser.grammar.clause.ClauseRequirementParser.CLAUSE_REQUIREMENT;
import static org.o42a.parser.grammar.clause.OutcomeParser.OUTCOME;
import static org.o42a.parser.grammar.clause.ReusedClauseParser.REUSED_CLAUSE;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.*;
import org.o42a.ast.clause.ClauseDeclaratorNode.Requirement;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.ArrayUtil;
import org.o42a.util.io.SourcePosition;


public class ClauseDeclaratorParser implements Parser<ClauseDeclaratorNode> {

	private final Grammar grammar;

	public ClauseDeclaratorParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public ClauseDeclaratorNode parse(ParserContext context) {
		if (context.next() != '<') {
			return null;
		}

		final SignNode<ClauseDeclaratorNode.Parenthesis> opening =
				opening(context);

		final ClauseIdNode clauseId = context.parse(CLAUSE_ID);

		if (clauseId == null) {
			return null;
		}

		final OutcomeNode outcome = context.parse(OUTCOME);
		final ReusedClauseNode[] reused = reused(context);
		final SignNode<Requirement> requirement =
				context.parse(CLAUSE_REQUIREMENT);
		final SignNode<ClauseDeclaratorNode.Parenthesis> closing =
				closing(context, opening);
		final StatementNode content = context.parse(this.grammar.statement());

		return new ClauseDeclaratorNode(
				opening,
				clauseId,
				outcome,
				reused,
				requirement,
				closing,
				content);
	}

	private SignNode<ClauseDeclaratorNode.Parenthesis> opening(
			ParserContext context) {

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<ClauseDeclaratorNode.Parenthesis> opening =
				new SignNode<>(
						start,
						context.current().fix(),
						ClauseDeclaratorNode.Parenthesis.OPENING);

		return context.skipComments(true, opening);
	}

	private ReusedClauseNode[] reused(ParserContext context) {

		ReusedClauseNode[] reused = new ReusedClauseNode[0];

		for (;;) {

			final ReusedClauseNode reusedClause = context.parse(REUSED_CLAUSE);

			if (reusedClause == null) {
				break;
			}

			reused = ArrayUtil.append(reused, reusedClause);
		}

		return reused;
	}

	private SignNode<ClauseDeclaratorNode.Parenthesis> closing(
			ParserContext context,
			SignNode<ClauseDeclaratorNode.Parenthesis> opening) {
		if (context.next() != '>') {
			context.getLogger().notClosed(opening, opening.getType().getSign());
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<ClauseDeclaratorNode.Parenthesis> closing =
				new SignNode<>(
						start,
						context.current().fix(),
						ClauseDeclaratorNode.Parenthesis.CLOSING);

		return context.acceptComments(false, closing);
	}

}
