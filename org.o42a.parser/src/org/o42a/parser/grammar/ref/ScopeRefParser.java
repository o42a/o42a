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
package org.o42a.parser.grammar.ref;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class ScopeRefParser implements Parser<ScopeRefNode> {

	public static final ScopeRefParser SCOPE_REF = new ScopeRefParser();

	private ScopeRefParser() {
	}

	@Override
	public ScopeRefNode parse(ParserContext context) {

		final FixedPosition start = context.current().fix();
		final ScopeType type;

		switch (context.next()) {
		case '*':
			type = ScopeType.IMPLIED;
			context.acceptAll();
			break;
		case '$':
			if (context.next() == '$') {
				type = ScopeType.ROOT;
				context.acceptAll();
				break;
			}
			type = ScopeType.MODULE;
			context.acceptButLast();
			break;
		case ':':
			switch (context.next()) {
			case ':':
				type = ScopeType.PARENT;
				if (context.next() == '=') {
					return null;
				}
				context.acceptButLast();
				break;
			case '=':
				return null;
			default:
				type = ScopeType.SELF;
				context.acceptButLast();
			}
			break;
		default:
			return null;
		}

		return context.acceptComments(
				false,
				new ScopeRefNode(start, context.current(), type));
	}

}
