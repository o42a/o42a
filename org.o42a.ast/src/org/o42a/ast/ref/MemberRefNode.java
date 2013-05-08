/*
    Abstract Syntax Tree
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarableNodeVisitor;


public class MemberRefNode
		extends AbstractRefNode
		implements DeclarableNode, ClauseIdNode {

	private final ExpressionNode owner;
	private final SignNode<Qualifier> qualifier;
	private final NameNode name;
	private final MembershipNode membership;

	public MemberRefNode(
			ExpressionNode owner,
			SignNode<Qualifier> qualifier,
			NameNode name,
			MembershipNode membership) {
		super(
				start(owner, name),
				end(qualifier, name, membership));
		this.owner = owner;
		this.name = name;
		this.qualifier = qualifier;
		this.membership = membership;
	}

	public final ExpressionNode getOwner() {
		return this.owner;
	}

	public final NameNode getName() {
		return this.name;
	}

	public final SignNode<Qualifier> getQualifier() {
		return this.qualifier;
	}

	public final MembershipNode getMembership() {
		return this.membership;
	}

	public final RefNode getDeclaredIn() {
		return this.membership != null ? this.membership.getDeclaredIn() : null;
	}

	@Override
	public final <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMemberRef(this, p);
	}

	@Override
	public final <R, P> R accept(DeclarableNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMemberRef(this, p);
	}

	@Override
	public final <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMemberRef(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return this;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return this;
	}

	@Override
	public final ScopeRefNode toScopeRef() {
		return null;
	}

	@Override
	public final MemberRefNode toMemberRef() {
		return this;
	}

	@Override
	public final AdapterRefNode toAdapterRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.owner != null) {
			this.owner.printContent(out);
		}
		if (this.qualifier != null) {
			this.qualifier.printContent(out);
		}
		if (this.name != null) {
			this.name.printContent(out);
		}
		if (this.membership != null) {
			this.membership.printContent(out);
		}
	}

	public enum Qualifier implements SignType {

		MEMBER() {

			@Override
			public String getSign() {
				return ":";
			}

		},

		MACRO() {

			@Override
			public String getSign() {
				return "#";
			}

		};

	}

}
