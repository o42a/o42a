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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.NoBoundNode;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.StatementNodeVisitor;
import org.o42a.ast.type.TypeArgumentNode;
import org.o42a.ast.type.TypeArgumentNodeVisitor;
import org.o42a.util.io.SourcePosition;


public class ScopeRefNode extends SignNode<ScopeType>
		implements RefNode, ClauseIdNode {

	public ScopeRefNode(
			SourcePosition start,
			SourcePosition end,
			ScopeType type) {
		super(start, end, type);
	}

	@Override
	public final <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public final <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public final <R, P> R accept(TypeArgumentNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public final <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return this;
	}

	@Override
	public final TypeArgumentNode toTypeArgument() {
		return this;
	}

	@Override
	public final ExpressionNode toExpression() {
		return this;
	}

	@Override
	public LocalNode toLocal() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return this;
	}

	@Override
	public final ScopeRefNode toScopeRef() {
		return this;
	}

	@Override
	public final MemberRefNode toMemberRef() {
		return null;
	}

	@Override
	public final AdapterRefNode toAdapterRef() {
		return null;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public final NoBoundNode toNoBound() {
		return null;
	}

}
