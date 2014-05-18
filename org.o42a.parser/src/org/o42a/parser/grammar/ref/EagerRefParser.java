/*
    Parser
    Copyright (C) 2014 Ruslan Lopatin

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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.EagerRefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class EagerRefParser implements Parser<EagerRefNode> {

	private final ExpressionNode owner;

	public EagerRefParser(ExpressionNode owner) {
		this.owner = owner;
	}

	@Override
	public EagerRefNode parse(ParserContext context) {
		if (context.next() != '>') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		if (context.next() != '>') {
			return null;
		}

		context.acceptAll();

		final SignNode<EagerRefNode.Suffix> suffix = new SignNode<>(
				start,
				context.current().fix(),
				EagerRefNode.Suffix.CHEVRON);

		return context.acceptComments(
				false,
				new EagerRefNode(this.owner, suffix));
	}

}
