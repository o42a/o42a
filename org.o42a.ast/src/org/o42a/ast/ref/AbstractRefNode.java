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

import org.o42a.ast.Node;
import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.type.TypeArgumentNode;
import org.o42a.ast.type.TypeArgumentNodeVisitor;
import org.o42a.util.io.SourcePosition;


public abstract class AbstractRefNode
		extends AbstractExpressionNode
		implements RefNode {

	public AbstractRefNode(SourcePosition start, SourcePosition end) {
		super(start, end);
	}

	public AbstractRefNode(Node... nodes) {
		super(nodes);
	}

	@Override
	public final <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public <R, P> R accept(TypeArgumentNodeVisitor<R, P> visitor, P p) {
		return accept((RefNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public final TypeArgumentNode toTypeArgument() {
		return this;
	}

	@Override
	public final RefNode toRef() {
		return this;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

}
