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
package org.o42a.parser.grammar.sentence;

import static org.o42a.parser.Grammar.ellipsis;
import static org.o42a.util.string.Characters.HORIZONTAL_ELLIPSIS;

import java.util.ArrayList;

import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.sentence.SerialNode.Separator;
import org.o42a.ast.statement.EllipsisNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.*;
import org.o42a.util.io.SourcePosition;


public class ConjunctionParser implements Parser<SerialNode[]> {

	private final Grammar grammar;

	public ConjunctionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public SerialNode[] parse(ParserContext context) {

		final Expectations expectations;

		if (this.grammar.isImperative()) {
			expectations =
					context.expect(',').expect((char) HORIZONTAL_ELLIPSIS);
		} else {
			expectations = context.expect(',');
		}

		final ArrayList<SerialNode> statements = new ArrayList<>();
		SignNode<Separator> separator = null;

		for (;;) {

			final SeparatorNodes separators = context.skipComments(true);
			final SourcePosition statementStart = context.current().fix();
			StatementNode stat =
					expectations.parse(this.grammar.statement());
			SerialNode statement;

			if (stat != null || separator != null) {
				statement = new SerialNode(separator, stat);
			} else {
				statement = new SerialNode(statementStart);
			}

			int c = context.next();

			if (this.grammar.isImperative()
					&& (c == '.' || c == HORIZONTAL_ELLIPSIS)) {

				final EllipsisNode ellipsis = context.push(ellipsis());

				if (ellipsis != null) {
					if (stat != null) {
						statements.add(statement);
					}
					stat = ellipsis;
					statement = new SerialNode(separator, ellipsis);
					ellipsis.addComments(separators);
					context.acceptAll();
					c = context.next();
				}
			}
			if (c == ',') {
				if (stat == null) {
					context.getLogger().emptyStatement(statementStart);
					statement.addComments(separators);
				}
				statements.add(statement);

				final SourcePosition separatorStart = context.current().fix();

				context.acceptAll();
				separator = new SignNode<>(
						separatorStart,
						context.current().fix(),
						Separator.THEN);
				context.acceptAll();
				context.acceptComments(true, separator);

				continue;
			}
			if (stat != null) {
				statements.add(statement);
				statement.addComments(separators);
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
