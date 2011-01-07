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
package org.o42a.ast.expression;

import org.o42a.ast.ref.TypeNode;
import org.o42a.ast.ref.TypeNodeVisitor;


public class AscendantsNode extends AbstractExpressionNode implements TypeNode {

	private final AscendantNode[] ascendants;

	public AscendantsNode(AscendantNode[] ascendants) {
		super(ascendants);
		this.ascendants = ascendants;
	}

	public AscendantNode[] getAscendants() {
		return this.ascendants;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAscendants(this, p);
	}

	@Override
	public final <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAscendants(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		for (AscendantNode ascendant : this.ascendants) {
			ascendant.printContent(out);
		}
	}

}
