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
package org.o42a.ast.statement;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.TypeNode;


public class DeclaratorNode extends AbstractStatementNode {

	private final DeclarableNode declarable;
	private final SignNode<DeclarationTarget> definitionAssignment;
	private final DefinitionCastNode definitionCast;
	private final ExpressionNode definition;

	public DeclaratorNode(
			DeclarableNode declarable,
			SignNode<DeclarationTarget> definitionAssignment,
			DefinitionCastNode definitionCast,
			ExpressionNode definition) {
		super(
				declarable.getStart(),
				end(
						definitionAssignment,
						definitionCast,
						definition));
		this.declarable = declarable;
		this.definitionAssignment = definitionAssignment;
		this.definitionCast = definitionCast;
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

	public DefinitionCastNode getDefinitionCast() {
		return this.definitionCast;
	}

	public final DefinitionKind getDefinitionKind() {

		final DefinitionCastNode cast = getDefinitionCast();

		return cast != null ? cast.getKind().getType() : null;
	}

	public final TypeNode getDefinitionType() {

		final DefinitionCastNode cast = getDefinitionCast();

		return cast != null ? cast.getType() : null;
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
		if (this.definitionCast != null) {
			this.definitionCast.printContent(out);
		}
		this.definition.printContent(out);
	}

}
