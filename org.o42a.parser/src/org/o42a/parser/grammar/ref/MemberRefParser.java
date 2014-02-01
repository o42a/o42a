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

import static org.o42a.parser.grammar.atom.NameParser.NAME;
import static org.o42a.parser.grammar.ref.MembershipParser.MEMBERSHIP;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.ast.ref.MembershipNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MemberRefParser implements Parser<MemberRefNode> {

	private static final QualifierParser QUALIFIER = new QualifierParser();

	private final ExpressionNode owner;
	private final boolean qualifierExpected;

	public MemberRefParser(ExpressionNode owner, boolean qualifierExpected) {
		this.owner = owner;
		this.qualifierExpected = qualifierExpected;
	}

	@Override
	public MemberRefNode parse(ParserContext context) {

		final SignNode<Qualifier> qualifier;

		if (qualifierExpected(context)) {
			qualifier = context.push(QUALIFIER);
			if (qualifier == null) {
				return null;
			}
		} else {
			qualifier = null;
		}

		final NameNode name = context.parse(NAME);

		if (name == null) {
			return null;
		}
		context.acceptComments(false, name);

		final MembershipNode membership = context.parse(MEMBERSHIP);

		return new MemberRefNode(this.owner, qualifier, name, membership);
	}

	private boolean qualifierExpected(ParserContext context) {
		if (this.qualifierExpected) {
			return true;
		}
		return this.owner != null && context.next() == '#';
	}

	private static final class QualifierParser
			implements Parser<SignNode<Qualifier>> {

		@Override
		public SignNode<Qualifier> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final Qualifier qualifier;

			switch (context.next()) {
			case ':':
				qualifier = Qualifier.MEMBER;
				context.acceptAll();
				break;
			case '#':
				if (context.next() == '#') {
					return null;
				}
				context.acceptButLast();
				qualifier = Qualifier.MACRO;
				break;
			default:
				return null;
			}

			return context.acceptComments(
					false,
					new SignNode<>(
							start,
							context.firstUnaccepted().fix(),
							qualifier));
		}

	}

}
