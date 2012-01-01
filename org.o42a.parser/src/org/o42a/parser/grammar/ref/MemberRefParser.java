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

import static org.o42a.parser.grammar.atom.NameParser.NAME;
import static org.o42a.parser.grammar.ref.RefParser.REF;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MemberRefParser implements Parser<MemberRefNode> {

	private static final QualifierParser QUALIFIER =
			new QualifierParser();
	private static final RetentionParser RETENTION =
			new RetentionParser();

	private final ExpressionNode owner;
	private final boolean qualifierExpected;

	public MemberRefParser(ExpressionNode owner, boolean qualifierExpected) {
		this.owner = owner;
		this.qualifierExpected = qualifierExpected;
	}

	@Override
	public MemberRefNode parse(ParserContext context) {

		final SignNode<Qualifier> qualifier;

		if (this.qualifierExpected) {
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

		final SignNode<MemberRetention> retention = context.parse(RETENTION);
		final RefNode declaredIn;

		if (retention == null) {
			declaredIn = null;
		} else {
			declaredIn = context.parse(REF);
			if (declaredIn == null) {
				context.getLogger().missingDeclaredIn(retention);
			}
		}

		return new MemberRefNode(
				this.owner,
				qualifier,
				name,
				retention,
				declaredIn);
	}

	private static final class QualifierParser
			implements Parser<SignNode<Qualifier>> {

		@Override
		public SignNode<Qualifier> parse(ParserContext context) {
			if (context.next() != ':') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			context.acceptAll();

			return context.acceptComments(
					false,
					new SignNode<Qualifier>(
							start,
							context.current().fix(),
							Qualifier.MEMBER_NAME));
		}

	}

	private static final class RetentionParser
			implements Parser<SignNode<MemberRetention>> {

		@Override
		public SignNode<MemberRetention> parse(ParserContext context) {
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
					new SignNode<MemberRetention>(
							start,
							context.current().fix(),
							MemberRetention.DECLARED_IN));
		}

	}

}
