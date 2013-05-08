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
package org.o42a.ast.type;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.RefNode;


public class AscendantsNode
		extends AbstractExpressionNode
		implements TypeArgumentNode {

	private final AscendantNode ancestor;
	private final AscendantNode[] samples;

	public AscendantsNode(AscendantNode ancestor, AscendantNode[] samples) {
		super(ancestor.getStart(), end(ancestor, lastNode(samples)));
		this.ancestor = ancestor;
		this.samples = samples;
	}

	public final AscendantNode getAncestor() {
		return this.ancestor;
	}

	public final boolean hasSamples() {
		return getSamples().length != 0;
	}

	public final AscendantNode[] getSamples() {
		return this.samples;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAscendants(this, p);
	}

	@Override
	public <R, P> R accept(TypeArgumentNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAscendants(this, p);
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
		if (!hasSamples()) {
			return this;
		}
		return null;
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
		this.ancestor.printContent(out);
		for (AscendantNode ascendant : this.samples) {
			ascendant.printContent(out);
		}
	}

}
