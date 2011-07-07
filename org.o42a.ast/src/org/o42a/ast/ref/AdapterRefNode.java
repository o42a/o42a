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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.ExpressionNode;


public class AdapterRefNode extends AbstractRefNode {

	private final ExpressionNode owner;
	private final SignNode<Qualifier> qualifier;
	private final RefNode type;
	private final SignNode<MemberRetention> retention;
	private final RefNode declaredIn;

	public AdapterRefNode(
			ExpressionNode owner,
			SignNode<Qualifier> qualifier,
			RefNode type,
			SignNode<MemberRetention> retention,
			RefNode declaredIn) {
		super(
				owner.getStart(),
				end(qualifier, type, retention, declaredIn));
		this.owner = owner;
		this.type = type;
		this.qualifier = qualifier;
		this.declaredIn = declaredIn;
		this.retention = retention;
	}

	public ExpressionNode getOwner() {
		return this.owner;
	}

	public RefNode getType() {
		return this.type;
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
		return visitor.visitAdapterRef(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.owner.printContent(out);
		this.qualifier.printContent(out);
		if (this.type != null) {
			this.type.printContent(out);
		}
		if (this.retention != null) {
			this.retention.printContent(out);
		}
		if (this.declaredIn != null) {
			this.declaredIn.printContent(out);
		}
	}

	public enum Qualifier implements SignType {

		FIELD_NAME;

		@Override
		public String getSign() {
			return "@@";
		}

	}

}
