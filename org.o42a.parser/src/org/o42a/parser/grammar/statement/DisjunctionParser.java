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

import java.util.ArrayList;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.AlternativeNode.Separator;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class DisjunctionParser implements Parser<AlternativeNode[]> {

	private final Grammar grammar;

	public DisjunctionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public AlternativeNode[] parse(ParserContext context) {

		final ArrayList<AlternativeNode> alternatives =
			new ArrayList<AlternativeNode>();
		SignNode<Separator> separatorSign = null;

		for (;;) {

			final CommentNode[] comments = context.skipComments();
			final FixedPosition conjunctionStart = context.current().fix();
			final SerialNode[] conjunction =
				context.parse(this.grammar.conjunction());
			final AlternativeNode alternative;

			if (conjunction != null) {
				alternative = new AlternativeNode(
						separatorSign,
						conjunction != null ? conjunction : new SerialNode[0]);

			} else {

				final FixedPosition start = context.current().fix();

				alternative = new AlternativeNode(start, start);
			}

			alternative.addComments(comments);
			context.skipComments(alternative);

			final Separator separator;
			final int c = context.next();

			if (c == ';') {
				separator = Separator.ALTERNATIVE;
			} else if (c == '|') {
				separator = Separator.OPPOSITE;
			} else {
				if (conjunction == null && alternatives.isEmpty()) {
					return null;
				}
				context.acceptButLast();
				alternatives.add(alternative);
				break;
			}
			alternatives.add(alternative);
			if (conjunction == null) {
				context.getLogger().emptyAlternative(conjunctionStart);
			}

			final FixedPosition separatorStart = context.current().fix();

			context.acceptAll();
			separatorSign = new SignNode<Separator>(
					separatorStart,
					context.current(),
					separator);
			context.acceptComments(separatorSign);
		}

		final int size = alternatives.size();

		if (size == 0) {
			return null;
		}

		return alternatives.toArray(new AlternativeNode[size]);
	}

}
