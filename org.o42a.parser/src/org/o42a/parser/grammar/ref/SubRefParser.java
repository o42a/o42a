/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.adapterRef;
import static org.o42a.parser.Grammar.deref;
import static org.o42a.parser.Grammar.memberRef;

import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class SubRefParser implements Parser<RefNode> {

	private final RefNode owner;
	private final boolean qualifierExpected;

	SubRefParser(RefNode owner, boolean qualifierExpected) {
		this.owner = owner;
		this.qualifierExpected = qualifierExpected;
	}

	@Override
	public RefNode parse(ParserContext context) {

		final RefNode owner;
		final boolean qualifierExpected;

		if (this.owner == null) {
			owner = null;
			qualifierExpected = false;
		} else {

			final int c = context.next();
			final int next;

			switch (c) {
			case '-':
				owner = context.parse(deref(this.owner));
				if (owner == null) {
					return null;
				}
				qualifierExpected = false;
				next = context.next();
				break;
			default:
				owner = this.owner;
				qualifierExpected = this.qualifierExpected;
				next = c;
			}
			if (next == '@') {
				return context.parse(adapterRef(owner));
			}
		}

		final MemberRefNode memberRef =
				context.parse(memberRef(owner, qualifierExpected));

		if (memberRef == null) {
			return this.owner != owner ? owner : null;
		}
		if (context.isEOF()) {
			return memberRef;
		}

		final RefNode childRef =
				context.parse(new SubRefParser(memberRef, true));

		return childRef != null ? childRef : memberRef;
	}

}
