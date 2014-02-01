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

import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ReusedClauseNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class ReusedClauseParser implements Parser<ReusedClauseNode> {

	static final ReusedClauseParser REUSED_CLAUSE = new ReusedClauseParser();

	private ReusedClauseParser() {
	}

	@Override
	public ReusedClauseNode parse(ParserContext context) {
		if (context.next() != '|') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<ReusedClauseNode.Separator> separator =
				new SignNode<>(
						start,
						context.current().fix(),
						ReusedClauseNode.Separator.OR);

		context.acceptComments(true, separator);

		if (context.next() == '*') {

			final SourcePosition asteriskStart = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					true,
					new ReusedClauseNode(
							separator,
							null,
							new SignNode<>(
									asteriskStart,
									context.current().fix(),
									ReusedClauseNode.ReuseContents.ASTERISK)));
		}

		final RefNode clause = context.parse(ref());

		if (clause == null) {
			context.getLogger().missingClause(separator);
		}

		if (context.next() == '*') {

			final SourcePosition asteriskStart = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					true,
					new ReusedClauseNode(
							separator,
							clause,
							new SignNode<>(
									asteriskStart,
									context.current().fix(),
									ReusedClauseNode.ReuseContents.ASTERISK)));
		}

		return context.acceptComments(
				true,
				new ReusedClauseNode(separator, clause, null));
	}

}
