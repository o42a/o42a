/*
    Parser
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.parser.grammar.field;

import static org.o42a.parser.grammar.field.TypeParser.TYPE;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.ast.field.TypeNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class ArrayTypeParser implements Parser<ArrayTypeNode> {

	public static final ArrayTypeParser ARRAY_TYPE = new ArrayTypeParser(null);

	private final TypeNode ancestor;

	ArrayTypeParser(TypeNode ancestor) {
		this.ancestor = ancestor;
	}

	@Override
	public ArrayTypeNode parse(ParserContext context) {
		if (context.next() != '[') {
			return null;
		}

		final SignNode<BracketsNode.Bracket> opening = opening(context);
		final TypeNode itemType = context.expect(']').parse(TYPE);
		final SignNode<BracketsNode.Bracket> closing =
				closing(context, opening);

		return context.acceptComments(
				false,
				new ArrayTypeNode(this.ancestor, opening, itemType, closing));
	}

	private SignNode<BracketsNode.Bracket> opening(ParserContext context) {

		final SourcePosition openingStart = context.current().fix();

		context.acceptAll();

		return context.acceptComments(
				true,
				new SignNode<BracketsNode.Bracket>(
						openingStart,
						context.current().fix(),
						BracketsNode.Bracket.OPENING_BRACKET));
	}

	private SignNode<BracketsNode.Bracket> closing(
			ParserContext context,
			SignNode<BracketsNode.Bracket> opening) {
		if (context.next() != ']') {
			context.getLogger().notClosed(
					opening,
					BracketsNode.Bracket.CLOSING_BRACKET.getSign());
			return null;
		}

		final SourcePosition closingStart = context.current().fix();

		context.acceptAll();

		return new SignNode<BracketsNode.Bracket>(
				closingStart,
				context.current().fix(),
				BracketsNode.Bracket.CLOSING_BRACKET);
	}

}
