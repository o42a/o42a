/*
    Abstract Syntax Tree
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
package org.o42a.ast.ref;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;


public class AdapterRefNode extends AbstractRefNode {

	private final ExpressionNode owner;
	private final AdapterIdNode adapterId;
	private final MembershipNode membership;

	public AdapterRefNode(
			ExpressionNode owner,
			AdapterIdNode adapterId,
			MembershipNode membership) {
		super(owner.getStart(), end(adapterId, membership));
		this.owner = owner;
		this.adapterId = adapterId;
		this.membership = membership;
	}

	public final ExpressionNode getOwner() {
		return this.owner;
	}

	public final AdapterIdNode getAdapterId() {
		return this.adapterId;
	}

	public final RefNode getType() {
		return this.adapterId.getType();
	}

	public final MembershipNode getMembership() {
		return this.membership;
	}

	public final RefNode getDeclaredIn() {
		return this.membership != null ? this.membership.getDeclaredIn() : null;
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAdapterRef(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final ScopeRefNode toScopeRef() {
		return null;
	}

	@Override
	public final MemberRefNode toMemberRef() {
		return null;
	}

	@Override
	public final AdapterRefNode toAdapterRef() {
		return this;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.owner.printContent(out);
		this.adapterId.printContent(out);
		if (this.membership != null) {
			this.membership.printContent(out);
		}
	}

}
