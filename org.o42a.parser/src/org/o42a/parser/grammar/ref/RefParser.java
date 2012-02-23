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
package org.o42a.parser.grammar.ref;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.ref.AscendantRefNode;
import org.o42a.ast.ref.IntrinsicRefNode;
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
			owner = context.parse(scopeRef());
			break;
		case ':':
			owner = parseAscendantRef(context, context.parse(scopeRef()));
			break;
		case '^':
			owner = context.parse(ascendantRef());
			break;
		case '$':

			final IntrinsicRefNode intrinsicRef = context.parse(intrinsicRef());

			if (intrinsicRef != null) {
				owner = parseAscendantRef(context, intrinsicRef);
			} else {
				owner = parseAscendantRef(context, context.parse(scopeRef()));
			}
			break;
		default:
			owner = parseAscendantRef(context, context.parse(parentRef()));
		}
		if (context.isEOF()) {
			return owner;
		}

		final RefNode ref = context.parse(new SubRefParser(owner, false));

		return ref != null ? ref : owner;
	}

	private static RefNode parseAscendantRef(
			ParserContext context,
			RefNode owner) {
		if (owner == null) {
			return null;
		}
		if (context.next() != '^') {
			return owner;
		}

		final AscendantRefNode ascendantRef =
				context.parse(ascendantRef(owner));

		if (ascendantRef != null) {
			return ascendantRef;
		}

		return owner;
	}

}
