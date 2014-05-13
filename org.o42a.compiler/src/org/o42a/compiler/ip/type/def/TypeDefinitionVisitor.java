/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.def;

import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


final class TypeDefinitionVisitor
		implements StatementNodeVisitor<Void, TypeDefinitionBuilder> {

	static final TypeDefinitionVisitor TYPE_DEFINITION_VISITOR =
			new TypeDefinitionVisitor();

	private TypeDefinitionVisitor() {
	}

	@Override
	public Void visitBraces(BracesNode braces, TypeDefinitionBuilder p) {
		p.addDefinitions(braces.getContent());
		return null;
	}

	@Override
	public Void visitDeclarator(
			DeclaratorNode declarator,
			TypeDefinitionBuilder p) {

		final ExpressionNode definitionNode = declarator.getDefinition();
		final DeclarationTarget target = declarator.getTarget();

		if (definitionNode == null && target != null) {
			return null;
		}
		if (target != null) {
			if (target.isStatic()) {
				p.getLogger().error(
						"prohibited_static_type_parameter",
						location(p, declarator.getTargetTypeNode()),
						"Type parameter can not be static");
			} else if (target.isPrototype()) {
				p.getLogger().error(
						"prohibited_prototype_type_parameter",
						location(p, declarator.getTargetTypeNode()),
						"Type parameter can not be a prototype");
			} else if (target.isAbstract()) {
				p.getLogger().error(
						"prohibited_abstract_type_parameter",
						location(p, declarator.getTargetTypeNode()),
						"Type parameter can not be abstract");
			}
		}

		final TypeParameterDeclaration parameter =
				new TypeParameterDeclaration(p, declarator);

		if (parameter.getDefinition() == null) {
			// TODO implement implied type parameters
			return null;
		}

		p.addParameter(parameter);

		return null;
	}

	@Override
	public Void visitStatement(
			StatementNode statement,
			TypeDefinitionBuilder p) {
		p.getLogger().error(
				"invalid_type_declaration",
				statement,
				"Not a valid type parameter declaration");
		return null;
	}

}
