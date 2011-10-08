/*
    Abstract Syntax Tree
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.ast.field;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.expression.BracketsNode.Bracket;


public class ArrayTypeNode
		extends AbstractNode
		implements TypeNode, AscendantSpecNode {

	private final TypeNode ancestor;
	private final SignNode<BracketsNode.Bracket> opening;
	private final TypeNode itemType;
	private final SignNode<BracketsNode.Bracket> closing;

	public ArrayTypeNode(
			TypeNode ancestor,
			SignNode<Bracket> opening,
			TypeNode itemType,
			SignNode<Bracket> closing) {
		super(
				start(ancestor, opening),
				end(ancestor, opening, itemType, closing));
		this.ancestor = ancestor;
		this.opening = opening;
		this.itemType = itemType;
		this.closing = closing;
	}

	public final TypeNode getAncestor() {
		return this.ancestor;
	}

	public SignNode<BracketsNode.Bracket> getOpening() {
		return this.opening;
	}

	public final TypeNode getItemType() {
		return this.itemType;
	}

	public final SignNode<BracketsNode.Bracket> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return visitor.visitArrayType(this, p);
	}

	@Override
	public <R, P> R accept(AscendantSpecNodeVisitor<R, P> visitor, P p) {
		return visitor.visitArrayType(this, p);
	}

	@Override
	public final <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return accept((TypeNodeVisitor<R, P>) visitor, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.ancestor != null) {
			this.ancestor.printContent(out);
		}
		if (this.itemType != null) {
			out.append('[');
			this.itemType.printContent(out);
			out.append(']');
		}
	}

}
