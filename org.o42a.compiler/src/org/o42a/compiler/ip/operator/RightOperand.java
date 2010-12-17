/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import static org.o42a.compiler.ip.member.DefinitionVisitor.DEFINITION_VISITOR;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.Statements;


class RightOperand extends BlockBuilder {

	RightOperand(CompilerContext context, ExpressionNode node) {
		super(context, node);
	}

	@Override
	public ExpressionNode getNode() {
		return (ExpressionNode) super.getNode();
	}

	@Override
	public void buildBlock(Block<?> block) {

		final Statements<?> statements = block.propose(this).alternative(this);
		final FieldDeclaration declaration =
			declaration(statements.nextDistributor()).override();

		statements.field(
				declaration,
				getNode().accept(DEFINITION_VISITOR, declaration));
	}

	protected FieldDeclaration declaration(Distributor distributor) {
		return fieldDeclaration(
				this,
				distributor,
				memberName("right_operand"))
				.setDeclaredIn(
						absolutePath(
								getContext(),
								"operators",
								"binary_operator")
						.target(getContext())
						.toStaticTypeRef());
	}

}
