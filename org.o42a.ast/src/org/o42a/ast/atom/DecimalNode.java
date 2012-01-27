/*
    Abstract Syntax Tree
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.util.io.SourcePosition;


public class DecimalNode
		extends AbstractExpressionNode
		implements AtomNode {

	private final String number;

	public DecimalNode(
			SourcePosition start,
			SourcePosition end,
			String number) {
		super(start, end);
		this.number = number;
	}

	public String getNumber() {
		return this.number;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitDecimal(this, p);
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitDecimal(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.number);
	}

}
