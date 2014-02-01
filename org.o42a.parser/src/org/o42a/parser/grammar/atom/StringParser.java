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
package org.o42a.parser.grammar.atom;

import static org.o42a.parser.grammar.atom.StringBoundParser.STRING_BOUND;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringBound;
import org.o42a.ast.atom.StringNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class StringParser implements Parser<StringNode> {

	public static final StringParser STRING = new StringParser();

	private StringParser() {
	}

	@Override
	public StringNode parse(ParserContext context) {

		final SignNode<StringBound> openingBound = context.parse(STRING_BOUND);

		if (openingBound == null) {
			return null;
		}
		if (openingBound.getType().isBlockBound()) {
			return context.parse(new TextBlockParser(openingBound));
		}

		return context.parse(new InlineStringParser(openingBound));

	}

}
