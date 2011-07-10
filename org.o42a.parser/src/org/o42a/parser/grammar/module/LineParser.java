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
package org.o42a.parser.grammar.module;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.module.LineType;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public abstract class LineParser<L extends LineType>
		implements Parser<SignNode<L>> {

	private final char lineChar;

	public LineParser(char lineChar) {
		this.lineChar = lineChar;
	}

	@Override
	public SignNode<L> parse(ParserContext context) {
		if (context.next() != this.lineChar) {
			return null;
		}

		final FixedPosition start = context.current().fix();

		context.skip();

		FixedPosition end = context.current().fix();

		while (context.next() == this.lineChar) {
			context.skip();
			end = context.current().fix();
		}

		final int length = end.column() - start.column();

		if (length < 3) {
			return null;
		}

		final L line = createLine(length);

		if (line == null) {
			return null;
		}

		context.acceptButLast();

		return context.acceptComments(false, new SignNode<L>(start, end, line));
	}

	protected abstract L createLine(int length);

}
