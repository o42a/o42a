/*
    Parser
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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.HORIZONTAL_ELLIPSIS;
import static org.o42a.parser.Grammar.IMPERATIVE;
import static org.o42a.parser.Grammar.ellipsis;

import java.util.ArrayList;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.sentence.SerialNode.Separator;
import org.o42a.ast.statement.EllipsisNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.*;


public class ConjunctionParser implements Parser<SerialNode[]> {

	private final Grammar grammar;

	public ConjunctionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SerialNode[] parse(ParserContext context) {

		final Expectations expectations;

		if (this.grammar == IMPERATIVE) {
			expectations =
				context.expect(',').expect((char) HORIZONTAL_ELLIPSIS);
		} else {
			expectations = context.expect(',');
		}

		final ArrayList<SerialNode> statements = new ArrayList<SerialNode>();
		SignNode<Separator> separator = null;

		for (;;) {

			final SeparatorNodes separators = context.skipComments(true);
			final FixedPosition statementStart = context.current().fix();
			final StatementNode stat =
				expectations.parse(this.grammar.statement());
			final SerialNode statement;

			if (stat != null || separator != null) {
				statement = new SerialNode(separator, stat);
			} else {
				statement = new SerialNode(statementStart);
			}

			final int c = context.next();

			if (c == ',') {
				if (stat == null) {
					context.getLogger().emptyStatement(statementStart);
					statement.addComments(separators);
				}
				statements.add(statement);

				final FixedPosition separatorStart = context.current().fix();

				context.acceptAll();
				separator = new SignNode<Separator>(
						separatorStart,
						context.current(),
						Separator.THEN);
				context.acceptAll();
				context.acceptComments(true, separator);

				continue;
			}
			if (this.grammar == IMPERATIVE
					&& (c == '.' || c == HORIZONTAL_ELLIPSIS)) {

				final EllipsisNode ellipsis = context.push(ellipsis());

				if (ellipsis != null) {
					if (stat != null) {
						statements.add(statement);
					}
					ellipsis.addComments(separators);
					statements.add(new SerialNode(null, ellipsis));
					context.acceptAll();
					break;
				}
			}
			if (stat != null) {
				statements.add(statement);
			}
			break;
		}

		final int size = statements.size();

		if (size == 0) {
			return null;
		}

		return statements.toArray(new SerialNode[size]);
	}

}
