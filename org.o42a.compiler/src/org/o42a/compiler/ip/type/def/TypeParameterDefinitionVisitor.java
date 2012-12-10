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

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.type.TypeRef;


final class TypeParameterDefinitionVisitor
		extends AbstractExpressionVisitor<TypeRef, Distributor> {

	private final TypeNodeVisitor<ParamTypeRef, Distributor> typeVisitor;

	TypeParameterDefinitionVisitor(TypeConsumer consumer) {
		this.typeVisitor = PLAIN_IP.typeIp().typeVisitor(consumer);
	}

	@Override
	public TypeRef visitAscendants(
			AscendantsNode ascendants,
			Distributor p) {
		return typeRef(ascendants, p);
	}

	@Override
	public TypeRef visitTypeParameters(
			TypeParametersNode parameters,
			Distributor p) {
		return typeRef(parameters, p);
	}

	@Override
	protected TypeRef visitRef(RefNode ref, Distributor p) {
		return typeRef(ref, p);
	}

	@Override
	public TypeRef visitMacroExpansion(
			MacroExpansionNode expansion,
			Distributor p) {
		return typeRef(expansion, p);
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

	private TypeRef typeRef(TypeNode node, Distributor p) {
		return node.accept(this.typeVisitor, p).parameterize();
	}

}
