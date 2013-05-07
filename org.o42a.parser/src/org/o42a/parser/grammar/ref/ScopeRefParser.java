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

import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class ScopeRefParser implements Parser<ScopeRefNode> {

	public static final ScopeRefParser SCOPE_REF = new ScopeRefParser();

	private ScopeRefParser() {
	}

	@Override
	public ScopeRefNode parse(ParserContext context) {

		final SourcePosition start = context.current().fix();
		final ScopeType type = parseType(context);

		if (type == null) {
			return null;
		}

		return context.acceptComments(
				false,
				new ScopeRefNode(start, context.current().fix(), type));
	}

	private ScopeType parseType(ParserContext context) {
		switch (context.next()) {
		case '*':
			context.acceptAll();
			return ScopeType.IMPLIED;
		case '#':
			if (context.next() != '#') {
				context.acceptButLast();
				return ScopeType.MACRO;
			}
			if (context.next() == '#') {
				// The macros scope requires exactly two hashes.
				return null;
			}
			context.acceptButLast();
			return ScopeType.MACROS;
		case '$':
			if (context.next() == '$') {
				context.acceptAll();
				return ScopeType.ANONYMOUS;
			}
			context.acceptButLast();
			return ScopeType.LOCAL;
		case '/':
			if (context.next() == '/') {
				context.acceptAll();
				return ScopeType.ROOT;
			}
			context.acceptButLast();
			return ScopeType.MODULE;
		case ':':
			switch (context.next()) {
			case ':':
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
				return ScopeType.PARENT;
			case '=':
				return null;
			default:
				context.acceptButLast();
				return ScopeType.SELF;
			}
		default:
			return null;
		}
	}

}
