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
import org.o42a.ast.clause.OutcomeNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class OutcomeParser implements Parser<OutcomeNode> {

	static final OutcomeParser OUTCOME = new OutcomeParser();

	private OutcomeParser() {
	}

	@Override
	public OutcomeNode parse(ParserContext context) {
		if (context.next() != '=') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<OutcomeNode.Prefix> prefix = context.acceptComments(
				true,
				new SignNode<>(
						start,
						context.current().fix(),
						OutcomeNode.Prefix.IS));
		final RefNode value = context.parse(ref());

		if (value == null) {
			context.getLogger().error(
					"missing_outcome_value",
					prefix,
					"Clause outcome value is missing");
		}

		return context.acceptComments(true, new OutcomeNode(prefix, value));
	}

}
