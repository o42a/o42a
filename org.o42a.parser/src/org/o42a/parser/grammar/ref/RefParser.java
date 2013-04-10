/*
    Parser
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.parentRef;
import static org.o42a.parser.Grammar.scopeRef;

import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class RefParser implements Parser<RefNode> {

	public static final RefParser REF = new RefParser();

	private RefParser() {
	}

	@Override
	public RefNode parse(ParserContext context) {

		final RefNode owner;

		switch (context.next()) {
		case '*':
		case ':':
		case '#':
		case '$':
		case '/':
			owner = context.parse(scopeRef());
			break;
		default:
			owner = context.parse(parentRef());
		}
		if (context.isEOF()) {
			return owner;
		}

		final RefNode ref = context.parse(new SubRefParser(owner, false));

		return ref != null ? ref : owner;
	}

}
