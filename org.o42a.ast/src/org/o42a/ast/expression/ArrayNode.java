/*
    Abstract Syntax Tree
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.ref.TypeNode;


public class ArrayNode extends AbstractExpressionNode {

	private final SignNode<Prefix> prefix;
	private final TypeNode itemType;
	private final BracketsNode items;

	public ArrayNode(
			SignNode<Prefix> prefix,
			TypeNode itemType,
			BracketsNode items) {
		super(prefix.getStart(), items.getEnd());
		this.prefix = prefix;
		this.itemType = itemType;
		this.items = items;
	}

	public SignNode<Prefix> getPrefix() {
		return this.prefix;
	}

	public TypeNode getItemType() {
		return this.itemType;
	}

	public BracketsNode getItems() {
		return this.items;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitArray(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.itemType != null) {
			this.itemType.printContent(out);
		}
		this.items.printContent(out);
	}

	public enum Prefix implements SignType {

		ARRAY() {

			@Override
			public String getSign() {
				return "*";
			}

		}

	}

}
