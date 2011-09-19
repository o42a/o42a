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
package org.o42a.ast.field;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.TypeNode;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class DeclaratorNode extends AbstractStatementNode {

	private final DeclarableNode declarable;
	private final SignNode<DeclarationTarget> definitionAssignment;
	private final InterfaceNode iface;
	private final ExpressionNode definition;

	public DeclaratorNode(
			DeclarableNode declarable,
			SignNode<DeclarationTarget> definitionAssignment,
			InterfaceNode iface,
			ExpressionNode definition) {
		super(
				declarable.getStart(),
				end(
						definitionAssignment,
						iface,
						definition));
		this.declarable = declarable;
		this.definitionAssignment = definitionAssignment;
		this.iface = iface;
		this.definition = definition;
	}

	public final DeclarableNode getDeclarable() {
		return this.declarable;
	}

	public final SignNode<DeclarationTarget> getDefinitionAssignment() {
		return this.definitionAssignment;
	}

	public final DeclarationTarget getTarget() {
		return this.definitionAssignment.getType();
	}

	public InterfaceNode getInterface() {
		return this.iface;
	}

	public final DefinitionKind getDefinitionKind() {

		final InterfaceNode iface = getInterface();

		return iface != null ? iface.getKind().getType() : null;
	}

	public final TypeNode getDefinitionType() {

		final InterfaceNode iface = getInterface();

		return iface != null ? iface.getType() : null;
	}

	public ExpressionNode getDefinition() {
		return this.definition;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitDeclarator(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.declarable.printContent(out);
		this.definitionAssignment.printContent(out);
		if (this.iface != null) {
			this.iface.printContent(out);
		}
		this.definition.printContent(out);
	}

}
