/*
    Compiler
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
package org.o42a.compiler.ip.field;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.st.StatementsAccess;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.source.CompilerContext;


public class FieldInterpreter {

	public static void field(
			Interpreter ip,
			CompilerContext context,
			DeclaratorNode declarator,
			StatementsAccess p) {

		final AccessDistributor distributor =
				p.nextDistributor().fromDeclaration();
		final FieldDeclaration declaration = declarator.getDeclarable().accept(
				new FieldDeclarableVisitor(ip, context, declarator),
				distributor);

		if (declaration == null) {
			return;
		}

		setDefinition(ip, p, declarator, declaration);
	}

	private static void setDefinition(
			Interpreter ip,
			StatementsAccess p,
			DeclaratorNode node,
			FieldDeclaration declaration) {
		if (declaration == null) {
			return;
		}

		final ExpressionNode definitionNode = node.getDefinition();

		if (definitionNode == null) {
			return;
		}

		final FieldDefinition definition = definitionNode.accept(
				ip.definitionVisitor(
						new FieldNesting(declaration).toTypeConsumer()),
				declaration);

		if (definition != null) {

			final FieldBuilder builder = p.get().field(declaration, definition);

			if (builder == null) {
				return;
			}

			p.statement(builder.build());
		}
	}

	private FieldInterpreter() {
	}

}
