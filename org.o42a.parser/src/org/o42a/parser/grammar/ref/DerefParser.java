/*
    Parser
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import org.o42a.ast.ref.DerefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class DerefParser implements Parser<DerefNode> {

	private final ExpressionNode owner;

	public DerefParser(ExpressionNode owner) {
		this.owner = owner;
	}

	@Override
	public DerefNode parse(ParserContext context) {
		if (context.next() != '-') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		if (context.next() != '>') {
			return null;
		}

		context.acceptAll();

		final SignNode<DerefNode.Suffix> suffix =
				new SignNode<>(
						start,
						context.current().fix(),
						DerefNode.Suffix.ARROW);

		return context.acceptComments(false, new DerefNode(this.owner, suffix));
	}

}
