/*
    Parser
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.parser.grammar.ref;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.AscendantRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class AscendantRefParser implements Parser<AscendantRefNode> {

	public static final AscendantRefParser ASCENDANT_REF =
		new AscendantRefParser(null);

	private final RefNode overridden;

	public AscendantRefParser(RefNode overridden) {
		this.overridden = overridden;
	}

	@Override
	public AscendantRefNode parse(ParserContext context) {
		if (context.next() != '^') {
			return null;
		}

		final FixedPosition prefixStart = context.current().fix();

		context.acceptAll();

		final SignNode<AscendantRefNode.Boundary> prefix =
			new SignNode<AscendantRefNode.Boundary>(
					prefixStart,
					context.current(),
					AscendantRefNode.Boundary.CIRCUMFLEX);

		context.acceptComments(prefix);

		final RefNode type = context.push(Grammar.ref());

		if (type == null || context.next() != '^') {
			return new AscendantRefNode(this.overridden, prefix, null, null);
		}

		final FixedPosition suffixStart = context.current().fix();

		context.acceptAll();

		final SignNode<AscendantRefNode.Boundary> suffix =
			new SignNode<AscendantRefNode.Boundary>(
					suffixStart,
					context.current(),
					AscendantRefNode.Boundary.CIRCUMFLEX);

		return context.acceptComments(new AscendantRefNode(
				this.overridden,
				prefix,
				type,
				suffix));
	}

}
