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
package org.o42a.ast.atom;

import org.o42a.ast.Position;
import org.o42a.ast.expression.ClauseNode;
import org.o42a.ast.expression.ClauseNodeVisitor;


public class NameNode extends AbstractAtomNode implements ClauseNode {

	private final String name;

	public NameNode(Position start, Position end, String name) {
		super(start, end);
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitName(this, p);
	}

	@Override
	public <R, P> R accept(ClauseNodeVisitor<R, P> visitor, P p) {
		return visitor.visitName(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.name);
	}

}
