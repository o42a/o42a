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
package org.o42a.ast.type;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ParenthesesNode.Parenthesis;


public class InterfaceNode extends AbstractNode {

	private final SignNode<Parenthesis> opening;
	private final SignNode<DefinitionKind> kind;
	private final TypeNode type;
	private final SignNode<Parenthesis> closing;

	public InterfaceNode(SignNode<DefinitionKind> kind) {
		super(kind.getStart(), kind.getEnd());
		this.opening = null;
		this.kind = kind;
		this.type = null;
		this.closing = null;
	}

	public InterfaceNode(
			SignNode<Parenthesis> opening,
			SignNode<DefinitionKind> kind,
			TypeNode declaredType,
			SignNode<Parenthesis> closing) {
		super(opening.getStart(), closing.getEnd());
		this.opening = opening;
		this.kind = kind;
		this.type = declaredType;
		this.closing = closing;
	}

	public SignNode<Parenthesis> getOpening() {
		return this.opening;
	}

	public SignNode<DefinitionKind> getKind() {
		return this.kind;
	}

	public TypeNode getType() {
		return this.type;
	}

	public SignNode<Parenthesis> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitInterface(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.opening == null) {
			this.kind.printContent(out);
			return;
		}
		out.append('(');
		this.kind.printContent(out);
		if (this.type != null) {
			this.type.printContent(out);
		}
		out.append(')');
	}

}
