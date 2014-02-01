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
package org.o42a.parser.grammar.type;

import static org.o42a.parser.Grammar.ref;

import java.util.ArrayList;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.AscendantNode;
import org.o42a.ast.type.AscendantNode.Separator;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


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

		AscendantNode ancestor = null;
		ArrayList<AscendantNode> samples = null;

		if (this.first != null) {
			ancestor = new AscendantNode(null, this.first);
		}

		for (;;) {

			final AscendantNode ascendant = context.parse(this.ascendantParser);

			if (ascendant == null) {
				break;
			}
			if (ancestor == null) {
				ancestor = ascendant;
			} else {
				if (samples == null) {
					samples = new ArrayList<>();
				}
				samples.add(ascendant);
			}
			if (ascendant.getSpec() == null) {
				break;
			}
		}

		if (ancestor == null) {
			return null;
		} else if (samples == null && this.first != null) {
			return null;
		}

		final AscendantNode[] arrayOfSamples;

		if (samples == null) {
			arrayOfSamples = new AscendantNode[0];
		} else {
			arrayOfSamples = samples.toArray(new AscendantNode[samples.size()]);
		}

		return new AscendantsNode(ancestor, arrayOfSamples);
	}

	private static final class AscendantParser
			implements Parser<AscendantNode> {

		@Override
		public AscendantNode parse(ParserContext context) {
			if (context.next() != '&') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			context.acceptAll();

			final SignNode<Separator> separator = new SignNode<>(
					start,
					context.current().fix(),
					Separator.SAMPLE);

			context.acceptComments(false, separator);

			final RefNode spec = context.parse(ref());

			if (spec == null) {
				context.getLogger().missingAscendantSpec(context.current());
			}

			return new AscendantNode(separator, spec);
		}

	}

}
