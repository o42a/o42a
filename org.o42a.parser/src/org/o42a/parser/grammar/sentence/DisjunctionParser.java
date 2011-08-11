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
package org.o42a.parser.grammar.sentence;

import java.util.ArrayList;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.AlternativeNode.Separator;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.parser.*;


public class DisjunctionParser implements Parser<AlternativeNode[]> {

	private final Grammar grammar;

	public DisjunctionParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public AlternativeNode[] parse(ParserContext context) {

		final Expectations expectations = context.expect(';').expect('|');
		final ArrayList<AlternativeNode> alternatives =
				new ArrayList<AlternativeNode>();
		SignNode<Separator> separatorSign = null;

		for (;;) {

			final SeparatorNodes separators = context.skipComments(true);
			final FixedPosition conjunctionStart = context.current().fix();
			final SerialNode[] alt =
					expectations.parse(this.grammar.conjunction());
			final AlternativeNode alternative;

			if (alt != null || separatorSign != null) {
				alternative = new AlternativeNode(
						separatorSign,
						alt != null ? alt : new SerialNode[0]);
			} else {

				final FixedPosition start = context.current().fix();

				alternative = new AlternativeNode(start, start);
			}

			final Separator separator;
			final int c = context.next();

			if (c == ';') {
				separator = Separator.ALTERNATIVE;
			} else if (c == '|') {
				separator = Separator.OPPOSITE;
			} else {
				if (alt != null) {
					alternatives.add(alternative);
				}
				break;
			}
			if (alt == null) {
				context.getLogger().emptyAlternative(conjunctionStart);
				alternative.addComments(separators);
			}
			alternatives.add(alternative);

			final FixedPosition separatorStart = context.current().fix();

			context.acceptAll();
			separatorSign = new SignNode<Separator>(
					separatorStart,
					context.current(),
					separator);
			context.acceptComments(true, separatorSign);
		}

		final int size = alternatives.size();

		if (size == 0) {
			return null;
		}

		return alternatives.toArray(new AlternativeNode[size]);
	}

}
