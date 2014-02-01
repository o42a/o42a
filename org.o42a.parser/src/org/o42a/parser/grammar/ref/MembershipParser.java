/*
    Parser
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MembershipNode;
import org.o42a.ast.ref.MembershipSign;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MembershipParser
		extends TypeRefParser<MembershipNode, MembershipSign> {

	public static final MembershipParser MEMBERSHIP = new MembershipParser();

	private MembershipParser() {
		super(new PrefixParser());
	}

	@Override
	protected MembershipNode createNode(
			SignNode<MembershipSign> prefix,
			SignNode<ParenthesisSign> opening,
			RefNode type,
			SignNode<ParenthesisSign> closing) {
		return new MembershipNode(prefix, opening, type, closing);
	}

	private static final class PrefixParser
			implements Parser<SignNode<MembershipSign>> {

		@Override
		public SignNode<MembershipSign> parse(ParserContext context) {
			if (context.next() != '@') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			if (context.next() == '@') {
				// adapter reference
				return null;
			}

			context.acceptButLast();

			return context.acceptComments(
					false,
					new SignNode<>(
							start,
							context.current().fix(),
							MembershipSign.DECLARED_IN));
		}

	}

}
