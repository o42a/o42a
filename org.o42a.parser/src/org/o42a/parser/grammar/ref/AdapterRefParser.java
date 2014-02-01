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

import static org.o42a.parser.grammar.ref.AdapterIdParser.ADAPTER_ID;
import static org.o42a.parser.grammar.ref.MembershipParser.MEMBERSHIP;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class AdapterRefParser implements Parser<AdapterRefNode> {

	private final ExpressionNode owner;

	public AdapterRefParser(ExpressionNode owner) {
		this.owner = owner;
	}

	@Override
	public AdapterRefNode parse(ParserContext context) {

		final AdapterIdNode adapterId = context.parse(ADAPTER_ID);

		if (adapterId == null) {
			return null;
		}

		final RefNode type = adapterId.getType();

		if (type == null) {
			return new AdapterRefNode(this.owner, adapterId, null);
		}
		if (adapterId.getOpening() != null) {

			final MembershipNode membership = context.parse(MEMBERSHIP);

			return new AdapterRefNode(this.owner, adapterId, membership);
		}

		final MemberRefNode memberRef = type.toMemberRef();

		if (memberRef != null) {
			if (memberRef.getMembership() != null) {

				final MemberRefNode newType = new MemberRefNode(
						memberRef.getOwner(),
						memberRef.getQualifier(),
						memberRef.getName(),
						null);
				final AdapterIdNode newAdapterId = new AdapterIdNode(
						adapterId.getPrefix(),
						null,
						newType,
						null);

				newAdapterId.addComments(adapterId.getComments());

				return new AdapterRefNode(
						this.owner,
						newAdapterId,
						memberRef.getMembership());
			}
		} else {

			final AdapterRefNode adapterRef = type.toAdapterRef();

			if (adapterRef != null) {
				return new AdapterRefNode(
						new AdapterRefNode(
								this.owner,
								new AdapterIdNode(
										adapterId.getPrefix(),
										null,
										adapterRef.getOwner().toRef(),
										null),
								null),
						new AdapterIdNode(
								adapterRef.getAdapterId().getPrefix(),
								adapterRef.getAdapterId().getOpening(),
								adapterRef.getType(),
								adapterRef.getAdapterId().getClosing()),
						adapterRef.getMembership());
			}
		}

		final MembershipNode membership = context.parse(MEMBERSHIP);

		return new AdapterRefNode(this.owner, adapterId, membership);
	}

}
