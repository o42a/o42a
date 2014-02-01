/*
    Abstract Syntax Tree
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
package org.o42a.ast.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;


public class DerefNode extends AbstractRefNode {

	private final ExpressionNode owner;
	private final SignNode<Suffix> suffix;

	public DerefNode(ExpressionNode owner, SignNode<Suffix> suffix) {
		super(owner.getStart(), suffix.getEnd());
		this.owner = owner;
		this.suffix = suffix;
	}

	public final ExpressionNode getOwner() {
		return this.owner;
	}

	public final SignNode<Suffix> getSuffix() {
		return this.suffix;
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitDeref(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.owner.printContent(out);
		this.suffix.printContent(out);
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
		return null;
	}

	public static enum Suffix implements SignType {

		ARROW() {

			@Override
			public String getSign() {
				return "->";
			}

		}

	}

}
