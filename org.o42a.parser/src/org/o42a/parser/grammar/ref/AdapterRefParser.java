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

import static org.o42a.parser.grammar.ref.RefParser.REF;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.ref.AdapterRefNode.Qualifier;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class AdapterRefParser implements Parser<AdapterRefNode> {

	private static final QualifierParser QUALIFIER = new QualifierParser();

	private final ExpressionNode owner;

	public AdapterRefParser(ExpressionNode owner) {
		this.owner = owner;
	}

	@Override
	public AdapterRefNode parse(ParserContext context) {

		final SignNode<Qualifier> qualifier = context.parse(QUALIFIER);

		if (qualifier == null) {
			return null;
		}

		final RefNode ref = context.parse(REF);

		if (ref == null) {
			context.getLogger().missingType(qualifier);
			return new AdapterRefNode(this.owner, qualifier, null, null, null);
		}

		if (ref instanceof MemberRefNode) {

			final MemberRefNode fieldRef = (MemberRefNode) ref;

			if (fieldRef.getRetention() != null) {

				final MemberRefNode type = new MemberRefNode(
						fieldRef.getOwner(),
						fieldRef.getQualifier(),
						fieldRef.getName(),
						null,
						null);

				return new AdapterRefNode(
						this.owner,
						qualifier,
						type,
						fieldRef.getRetention(),
						fieldRef.getDeclaredIn());
			}
		} else if (ref instanceof AdapterRefNode) {

			final AdapterRefNode adapterRef = (AdapterRefNode) ref;

			return new AdapterRefNode(
					new AdapterRefNode(
							this.owner,
							qualifier,
							(RefNode) adapterRef.getOwner(),
							null,
							null),
					adapterRef.getQualifier(),
					adapterRef.getType(),
					adapterRef.getRetention(),
					adapterRef.getDeclaredIn());
		}

		return new AdapterRefNode(this.owner, qualifier, ref, null, null);
	}

	private static final class QualifierParser
			implements Parser<SignNode<Qualifier>> {

		@Override
		public SignNode<Qualifier> parse(ParserContext context) {
			if (context.next() != '@') {
				return null;
			}

			final SourcePosition start = context.current().fix();

			if (context.next() != '@') {
				return null;
			}
			context.acceptAll();

			return context.acceptComments(
					false,
					new SignNode<Qualifier>(
							start,
							context.current().fix(),
							Qualifier.FIELD_NAME));
		}

	}

}
