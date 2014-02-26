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

import static org.o42a.common.macro.Macros.expandMacro;
import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.PLAIN_REF_IP;
import static org.o42a.compiler.ip.type.TypeInterpreter.redundantTypeArguments;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;


final class TypeParameterDefinitionVisitor
		extends AbstractExpressionVisitor<TypeRef, AccessDistributor> {

	private final TypeConsumer consumer;
	private final TypeRefParameters typeParameters;

	TypeParameterDefinitionVisitor(TypeConsumer consumer) {
		this.consumer = consumer;
		this.typeParameters = null;
	}

	private TypeParameterDefinitionVisitor(
			TypeConsumer consumer,
			TypeRefParameters typeParameters) {
		this.typeParameters = typeParameters;
		this.consumer = consumer;
	}

	@Override
	public TypeRef visitTypeArguments(
			TypeArgumentsNode arguments,
			AccessDistributor p) {

		final ExpressionNode ascendantNode = arguments.getType();

		if (ascendantNode == null) {
			return super.visitTypeArguments(arguments, p);
		}

		final TypeRefParameters typeParameters;

		if (this.typeParameters != null) {
			redundantTypeArguments(p.getLogger(), arguments);
			typeParameters = this.typeParameters;
		} else {
			typeParameters = PLAIN_IP.typeIp().typeArguments(
					arguments,
					p,
					this.consumer);
			if (typeParameters == null) {
				return null;
			}
		}

		return ascendantNode.accept(
				new TypeParameterDefinitionVisitor(
						this.consumer.noConsumption(),
						typeParameters),
				p);
	}

	@Override
	protected TypeRef visitRef(RefNode node, AccessDistributor p) {

		final Owner ref =
				node.accept(PLAIN_REF_IP.ownerVisitor(), p.fromDeclaration());

		if (ref == null) {
			return null;
		}

		if (!ref.isMacroExpanding()) {
			return typeRef(this.consumer.consumeType(
					ref.targetRef(),
					this.typeParameters));
		}

		return typeRef(this.consumer.consumeType(
				expandMacro(ref.targetRef()),
				this.typeParameters));
	}

	@Override
	protected TypeRef visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {
		p.getLogger().error(
				"invalid_type_parameter",
				location(p, expression),
				"Invalid type parameter definition");
		return null;
	}

	private static TypeRef typeRef(ParamTypeRef param) {
		if (param == null) {
			return null;
		}
		return param.parameterize();
	}

}
