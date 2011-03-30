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
package org.o42a.parser.grammar.atom;

import static java.lang.Character.isWhitespace;
import static org.o42a.parser.Grammar.isDigit;

import org.o42a.ast.EmptyNode;
import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.DecimalNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class DecimalParser implements Parser<DecimalNode> {

	public static final DecimalParser DECIMAL = new DecimalParser();

	private DecimalParser() {
	}

	@Override
	public DecimalNode parse(ParserContext context) {

		FixedPosition spaceStart = null;
		boolean wrongSpace = false;
		FixedPosition start = null;
		StringBuilder number = null;

		for (;;) {

			final int c = context.next();

			if (isDigit(c)) {
				if (number == null) {
					start = context.current().fix();
					number = new StringBuilder();
				} else {
					if (wrongSpace) {
						context.getLogger().invalidSpaceInNumber(
								new EmptyNode(spaceStart, context.current()));
						wrongSpace = false;
					}
					spaceStart = null;
				}
				number.append((char) c);
				continue;
			}
			if (isWhitespace(c) && c != '\n') {
				if (spaceStart == null) {
					spaceStart = context.current().fix();
					if (Character.getType(c) != Character.SPACE_SEPARATOR) {
						wrongSpace = true;
					}
				} else {
					wrongSpace = true;
				}
				continue;
			}
			if (number == null) {
				return null;
			}
			context.acceptButLast();

			final DecimalNode result = new DecimalNode(
					start,
					context.firstUnaccepted(),
					number.toString());

			return context.acceptComments(false, result);
		}
	}

}
