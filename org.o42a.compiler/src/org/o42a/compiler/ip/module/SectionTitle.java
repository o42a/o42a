/*
    Compiler
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
package org.o42a.compiler.ip.module;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.module.SectionAscendantsVisitor.SECTION_ASCENDANTS_VISITOR;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.statement.DeclaratorNode;
import org.o42a.compiler.ip.member.FieldDeclarableVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDeclaration;


final class SectionTitle {

	private final Node node;
	private final DeclaratorNode declarator;

	SectionTitle(SectionNode sectionNode) {
		this.declarator = sectionNode.getDeclarator();
		if (this.declarator != null) {
			this.node = this.declarator;
		} else {
			this.node = sectionNode.getTitle();
		}
	}

	public final Node getNode() {
		return this.node;
	}

	public final DeclaratorNode getDeclarator() {
		return this.declarator;
	}

	public final boolean isValid() {
		return this.declarator != null;
	}

	public FieldDeclaration declaration(Distributor distributor) {
		if (!isValid()) {
			return null;
		}

		final FieldDeclarableVisitor visitor = new FieldDeclarableVisitor(
				PLAIN_IP,
				distributor.getContext(),
				getDeclarator());

		return getDeclarator().getDeclarable().accept(visitor, distributor);
	}

	public AscendantsDefinition ascendants(Distributor distributor) {
		if (!isValid()) {
			return null;
		}

		final ExpressionNode definition = getDeclarator().getDefinition();

		if (definition == null) {
			return null;
		}

		return definition.accept(SECTION_ASCENDANTS_VISITOR, distributor);
	}

	@Override
	public String toString() {
		if (this.node == null) {
			return super.toString();
		}
		return this.node.toString();
	}

}
