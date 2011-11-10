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

import static org.o42a.parser.Grammar.isDigit;

import org.o42a.ast.atom.DecimalNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;


public class DecimalParser implements Parser<DecimalNode> {

	public static final DecimalParser DECIMAL = new DecimalParser();

	private DecimalParser() {
	}

	@Override
	public DecimalNode parse(ParserContext context) {

		SourcePosition spaceStart = null;
		boolean wrongSpace = false;
		SourcePosition start = null;
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
								new SourceRange(
										spaceStart,
										context.current().fix()));
						wrongSpace = false;
					}
					spaceStart = null;
				}
				number.append((char) c);
				continue;
			}
			if (Character.getType(c) == Character.SPACE_SEPARATOR) {
				if (spaceStart == null) {
					spaceStart = context.current().fix();
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
					context.firstUnaccepted().fix(),
					number.toString());

			return context.acceptComments(false, result);
		}
	}

}
