/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.ref.RefInterpreter.PLAIN_REF_IP;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class TypeParameterDefinitionVisitor
		extends AbstractExpressionVisitor<TypeRef, Distributor> {

	static final TypeParameterDefinitionVisitor
	TYPE_PARAMETER_DEFINITION_VISITOR = new TypeParameterDefinitionVisitor();

	private TypeParameterDefinitionVisitor() {
	}

	@Override
	protected TypeRef visitRef(RefNode ref, Distributor p) {

		final Ref typeRef = ref.accept(PLAIN_REF_IP.bodyRefVisitor(), p);

		if (typeRef == null) {
			return null;
		}

		return typeRef.toTypeRef();
	}

	@Override
	protected TypeRef visitExpression(
			ExpressionNode expression,
			Distributor p) {
		p.getLogger().error(
				"invalid_type_parameter_definition",
				location(p, expression),
				"Type parameter desfinition should be a type reference");
		return null;
	}

}
