/*
    Parser
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.file;

import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;


class OddParser implements Parser<SourceRange> {

	public static final OddParser ODD = new OddParser();

	private OddParser() {
	}

	@Override
	public SourceRange parse(ParserContext context) {

		SourcePosition start = null;
		SourcePosition end;

		for (;;) {

			final int next = context.next();

			if (next < 0) {
				if (start == null) {
					return null;
				}
				end = context.current().fix();
				context.acceptAll();
				break;
			}
			if (next == '\n') {
				if (start == null) {
					return null;
				}
				end = context.current().fix();
				context.acceptButLast();
				break;
			}
			if (start == null) {
				start = context.current().fix();
			}
		}

		final SourceRange result = new SourceRange(start, end);

		context.getLogger().odd(result);

		return result;
	}

}
