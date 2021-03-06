/*
    Abstract Syntax Tree
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.ast.phrase;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.util.io.SourcePosition;


public final class NoBoundNode
		extends SignNode<BoundSign>
		implements BoundNode {

	public NoBoundNode(SourcePosition start, SourcePosition end) {
		super(start, end, BoundSign.NO_BOUND);
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final NoBoundNode toNoBound() {
		return this;
	}

}
