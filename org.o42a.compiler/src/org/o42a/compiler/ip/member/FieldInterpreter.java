/*
    Compiler
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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.member.DefinitionVisitor.DEFINITION_VISITOR;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.DeclaratorNode;
import org.o42a.compiler.ip.RefVisitor;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.st.sentence.Statements;


public class FieldInterpreter {

	static final AdapterFieldVisitor ADAPTER_FIELD_VISITOR =
		new AdapterFieldVisitor();

	public static Ref field(
			CompilerContext context,
			DeclaratorNode declarator,
			Statements<?> p) {

		final Distributor distributor = p.nextDistributor();
		final FieldDeclaration declaration = declarator.getDeclarable().accept(
				new FieldDeclarableVisitor(context, declarator),
				distributor);

		if (declaration == null) {
			return null;
		}

		return setDefinition(p, declarator, declaration);
	}

	private static Ref setDefinition(
			Statements<?> p,
			DeclaratorNode node,
			FieldDeclaration declaration) {
		if (declaration == null) {
			return null;
		}

		final ExpressionNode definitionNode = node.getDefinition();

		if (definitionNode == null) {
			return null;
		}

		final FieldDefinition definition =
			definitionNode.accept(DEFINITION_VISITOR, declaration);

		if (definition != null) {

			final FieldBuilder builder = p.field(declaration, definition);

			if (builder == null) {
				return null;
			}

			p.statement(builder.build());
		}

		return null;
	}

	private FieldInterpreter() {
	}

	private static final class AdapterFieldVisitor extends RefVisitor {

		@Override
		protected StaticTypeRef declaredIn(
				RefNode declaredInNode,
				Distributor p) {
			return null;
		}

	}

}
