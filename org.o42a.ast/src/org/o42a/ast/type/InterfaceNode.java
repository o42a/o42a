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
import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;


public class InterfaceNode extends AbstractNode {

	public static final TypeParameterNode[] NO_PARAMETER =
			new TypeParameterNode[0];

	private final SignNode<ParenthesisSign> opening;
	private final SignNode<DefinitionKind> kind;
	private final TypeParameterNode[] parameters;
	private final SignNode<ParenthesisSign> closing;

	public InterfaceNode(SignNode<DefinitionKind> kind) {
		super(kind.getStart(), kind.getEnd());
		this.opening = null;
		this.kind = kind;
		this.parameters = NO_PARAMETER;
		this.closing = null;
	}

	public InterfaceNode(
			SignNode<ParenthesisSign> opening,
			SignNode<DefinitionKind> kind,
			TypeParameterNode[] parameters,
			SignNode<ParenthesisSign> closing) {
		super(opening.getStart(), closing.getEnd());
		this.opening = opening;
		this.kind = kind;
		this.parameters = parameters;
		this.closing = closing;
	}

	public SignNode<ParenthesisSign> getOpening() {
		return this.opening;
	}

	public SignNode<DefinitionKind> getKind() {
		return this.kind;
	}

	public TypeParameterNode[] getParameters() {
		return this.parameters;
	}

	public SignNode<ParenthesisSign> getClosing() {
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

		final int numTypes = this.parameters.length;

		if (numTypes > 0) {
			this.parameters[0].printContent(out);
			for (int i = 1; i < numTypes; ++i) {
				out.append(',');
				this.parameters[i].printContent(out);
			}
		}
		out.append(')');
	}

}
