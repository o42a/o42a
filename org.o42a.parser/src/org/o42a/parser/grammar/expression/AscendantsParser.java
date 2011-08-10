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
package org.o42a.parser.grammar.expression;

import java.util.ArrayList;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.AscendantNode;
import org.o42a.ast.expression.AscendantNode.AscendantType;
import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class AscendantsParser implements Parser<AscendantsNode> {

	public static final AscendantsParser SAMPLES = new AscendantsParser(null);

	private final RefNode first;
	private final AscendantParser ascendantParser;

	public AscendantsParser(RefNode first) {
		this.first = first;
		this.ascendantParser = new AscendantParser();
	}

	@Override
	public AscendantsNode parse(ParserContext context) {

		ArrayList<AscendantNode> ascendants = null;

		if (this.first != null) {
			ascendants = new ArrayList<AscendantNode>();
			ascendants.add(new AscendantNode(null, this.first));
		}

		for (;;) {

			final AscendantNode ascendant = context.parse(this.ascendantParser);

			if (ascendant == null) {
				break;
			}
			if (ascendants == null) {
				ascendants = new ArrayList<AscendantNode>();
			}
			ascendants.add(ascendant);
			if (ascendant.getAscendant() == null) {
				break;
			}
		}

		if (ascendants == null) {
			return null;
		}

		final int size = ascendants.size();

		if (this.first != null && size == 1) {
			return null;
		}

		return new AscendantsNode(ascendants.toArray(new AscendantNode[size]));
	}

	private static final class AscendantParser
			implements Parser<AscendantNode> {

		@Override
		public AscendantNode parse(ParserContext context) {
			if (context.next() != '&') {
				return null;
			}

			final FixedPosition start = context.current().fix();

			context.acceptAll();

			final SignNode<AscendantType> separator =
					new SignNode<AscendantType>(
							start,
							context.current(),
							AscendantType.SAMPLE);

			context.acceptComments(false, separator);

			final RefNode sample = context.parse(Grammar.ref());

			if (sample == null) {
				context.getLogger().missingSample(context.current());
			}

			return new AscendantNode(separator, sample);
		}

	}

}
