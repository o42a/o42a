/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRefParameters;


final class SectionAscendantsVisitor
		implements ExpressionNodeVisitor<
				AscendantsDefinition,
				AccessDistributor> {

	private final TypeConsumer consumer;

	SectionAscendantsVisitor(TypeConsumer consumer) {
		this.consumer = consumer;
	}

	@Override
	public AscendantsDefinition visitTypeArguments(
			TypeArgumentsNode arguments,
			AccessDistributor p) {

		AscendantsDefinition ascendants =
				new AscendantsDefinition(location(p, arguments), p);
		final TypeRefParameters typeArguments =
				PLAIN_IP.typeIp().typeArguments(
						arguments,
						p,
						this.consumer);
		final ExpressionNode ascendantNode = arguments.getType();

		if (ascendantNode != null) {

			final Ref ascendantRef = ascendantNode.accept(
					PLAIN_IP.expressionVisitor(),
					p.fromDeclaration());

			if (ascendantRef != null) {

				final ParamTypeRef type =
						this.consumer.consumeType(ascendantRef, typeArguments);

				if (type != null) {
					ascendants = type.updateAncestor(ascendants);
				}
			}
		}

		return ascendants;
	}

	@Override
	public AscendantsDefinition visitScopeRef(ScopeRefNode ref, AccessDistributor p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return new AscendantsDefinition(location(p, ref), p);
		}
		return visitRef(ref, p);
	}

	@Override
	public AscendantsDefinition visitParentheses(
			ParenthesesNode parentheses,
			AccessDistributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return visitExpression(parentheses, p);
	}

	@Override
	public AscendantsDefinition visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {

		final Ref ref = expression.accept(
				PLAIN_IP.expressionVisitor(),
				p.fromDeclaration());

		if (ref == null) {
			return null;
		}

		return new AscendantsDefinition(ref, p, ref.toTypeRef());
	}

}
