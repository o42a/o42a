/*
    Parser
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.ast.ref.*;
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

		final RefNode ref = context.parse(new QNameParser(owner, false));

		return ref != null ? ref : owner;
	}

	private RefNode parseAscendantRef(ParserContext context, RefNode owner) {
		if (owner == null) {
			return null;
		}
		if (context.next() != '^') {
			return owner;
		}

		final AscendantRefNode ascendantRef = context.parse(ascendantRef(owner));

		if (ascendantRef != null) {
			return ascendantRef;
		}

		return owner;
	}

	private static final class QNameParser implements Parser<RefNode> {

		private final RefNode owner;
		private final boolean qualifierExpected;

		public QNameParser(RefNode owner, boolean qualifierExpected) {
			this.owner = owner;
			this.qualifierExpected = qualifierExpected;
		}

		@Override
		public RefNode parse(ParserContext context) {
			if (this.owner != null && context.next() == '@') {
				return context.parse(new AdapterRefParser(this.owner));
			}

			final MemberRefParser fieldRefParser =
				new MemberRefParser(this.owner, this.qualifierExpected);
			final MemberRefNode ref = context.parse(fieldRefParser);

			if (ref == null) {
				return null;
			}
			if (context.isEOF()) {
				return ref;
			}

			final RefNode childRef = context.parse(new QNameParser(ref, true));

			return childRef != null ? childRef : ref;
		}

	}

}
