/*
    Parser
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.ast.ref.BodyRefNode;
import org.o42a.ast.ref.BodyRefNode.Suffix;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class BodyRefParser implements Parser<BodyRefNode> {

	private final ExpressionNode owner;

	public BodyRefParser(ExpressionNode owner) {
		this.owner = owner;
	}

	@Override
	public BodyRefNode parse(ParserContext context) {
		if (context.next() != '`') {
			return null;
		}

		final SourcePosition suffixStart = context.current().fix();

		context.acceptAll();

		final SignNode<Suffix> suffix = new SignNode<BodyRefNode.Suffix>(
				suffixStart,
				context.current().fix(),
				BodyRefNode.Suffix.BACKQUOTE);
		final BodyRefNode bodyRef = new BodyRefNode(this.owner, suffix);

		return context.acceptComments(false, bodyRef);
	}

}
