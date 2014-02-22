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
package org.o42a.ast.type;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.RefNode;


public class StaticRefNode
		extends AbstractExpressionNode
		implements TypeArgumentNode {

	private final SignNode<Prefix> prefix;
	private final RefNode ref;

	public StaticRefNode(SignNode<Prefix> prefix, RefNode ref) {
		super(prefix.getStart(), end(prefix, ref));
		this.prefix = prefix;
		this.ref = ref;
	}

	public final SignNode<Prefix> getPrefix() {
		return this.prefix;
	}

	public final RefNode getRef() {
		return this.ref;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitStaticRef(this, p);
	}

	@Override
	public <R, P> R accept(TypeArgumentNodeVisitor<R, P> visitor, P p) {
		return visitor.visitStaticRef(this, p);
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
	public final TypeArgumentNode toTypeArgument() {
		return this;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.ref != null) {
			this.ref.printContent(out);
		}
	}

	public enum Prefix implements SignType {

		STATIC_REF() {
			@Override
			public String getSign() {
				return "&";
			}
		}

	}

}
