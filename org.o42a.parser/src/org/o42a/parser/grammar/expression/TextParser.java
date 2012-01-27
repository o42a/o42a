/*
    Parser
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.ast.atom.StringNode;
import org.o42a.ast.expression.TextNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.parser.grammar.atom.StringLiteralParser;


public class TextParser implements Parser<TextNode> {

	public static final TextParser TEXT = new TextParser();

	private TextParser() {
	}

	@Override
	public TextNode parse(ParserContext context) {

		ArrayList<StringNode> literals = null;

		for (;;) {

			final StringNode literal =
					context.parse(StringLiteralParser.STRING_LITERAL);

			if (literal == null) {
				break;
			}
			if (literals == null) {
				literals = new ArrayList<StringNode>();
			}
			literals.add(literal);
		}
		if (literals == null) {
			return null;
		}

		return new TextNode(literals.toArray(new StringNode[literals.size()]));
	}

}
