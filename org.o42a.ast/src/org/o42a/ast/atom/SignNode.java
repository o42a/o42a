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
package org.o42a.ast.atom;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.util.io.SourcePosition;


public class SignNode<S extends SignType>
		extends AbstractNode
		implements AtomNode {

	private final S type;

	public SignNode(SourcePosition start, SourcePosition end, S type) {
		super(start, end);
		this.type = type;
	}

	public S getType() {
		return this.type;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitSign(this, p);
	}

	@Override
	public final <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitSign(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.type.getSign());
	}

}
