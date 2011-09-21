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

import org.o42a.ast.NodeVisitor;
import org.o42a.ast.Position;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseKeyNode;
import org.o42a.ast.clause.ClauseKeyNodeVisitor;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.TypeNodeVisitor;
import org.o42a.ast.statement.StatementNodeVisitor;


public class ScopeRefNode extends SignNode<ScopeType>
		implements RefNode, ClauseKeyNode {

	public ScopeRefNode(Position start, Position end, ScopeType type) {
		super(start, end, type);
	}

	@Override
	public final <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public final <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

	@Override
	public final <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public final <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public <R, P> R accept(ClauseKeyNodeVisitor<R, P> visitor, P p) {
		return visitor.visitScopeRef(this, p);
	}

}
