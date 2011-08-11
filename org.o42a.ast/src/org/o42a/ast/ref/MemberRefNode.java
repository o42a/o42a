/*
    Abstract Syntax Tree
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import org.o42a.ast.clause.ClauseKeyNodeVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarableNodeVisitor;


public class MemberRefNode extends AbstractRefNode implements DeclarableNode {

	private final ExpressionNode owner;
	private final SignNode<Qualifier> qualifier;
	private final NameNode name;
	private final SignNode<MemberRetention> retention;
	private final RefNode declaredIn;

	public MemberRefNode(
			ExpressionNode owner,
			SignNode<Qualifier> qualifier,
			NameNode name,
			SignNode<MemberRetention> retention,
			RefNode declaredIn) {
		super(
				start(owner, name),
				end(qualifier, name, retention, declaredIn));
		this.owner = owner;
		this.name = name;
		this.qualifier = qualifier;
		this.declaredIn = declaredIn;
		this.retention = retention;
	}

	public ExpressionNode getOwner() {
		return this.owner;
	}

	public NameNode getName() {
		return this.name;
	}

	public SignNode<Qualifier> getQualifier() {
		return this.qualifier;
	}

	public RefNode getDeclaredIn() {
		return this.declaredIn;
	}

	public SignNode<MemberRetention> getRetention() {
		return this.retention;
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMemberRef(this, p);
	}

	@Override
	public final <R, P> R accept(ClauseKeyNodeVisitor<R, P> visitor, P p) {
		return accept((DeclarableNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public <R, P> R accept(DeclarableNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMemberRef(this, p);
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
		if (this.retention != null) {
			this.retention.printContent(out);
		}
		if (this.declaredIn != null) {
			this.declaredIn.printContent(out);
		}
	}

	public enum Qualifier implements SignType {

		MEMBER_NAME;

		@Override
		public String getSign() {
			return ":";
		}

	}

}
