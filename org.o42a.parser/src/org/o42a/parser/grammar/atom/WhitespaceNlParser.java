/*
    Parser
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class WhitespaceNlParser implements Parser<Object> {

	public static final WhitespaceNlParser WHITESPACE_NL =
			new WhitespaceNlParser();

	private WhitespaceNlParser() {
	}

	@Override
	public Object parse(ParserContext context) {

		boolean whitespacePresent = false;

		for (;;) {

			final int c = context.next();

			if (Character.isWhitespace(c)) {
				whitespacePresent = true;
				continue;
			}
			context.acceptButLast();
			break;
		}

		return whitespacePresent ? Boolean.TRUE : null;
	}

}
