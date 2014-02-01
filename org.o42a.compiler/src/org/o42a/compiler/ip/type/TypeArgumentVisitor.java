/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.type;

import static org.o42a.common.macro.Macros.expandMacro;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.singleAlt;
import static org.o42a.compiler.ip.Interpreter.singleExpression;
import static org.o42a.compiler.ip.type.TypeInterpreter.redundantTypeArguments;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.type.*;
import org.o42a.common.ref.ArbitraryTypeRefParameters;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.param.TypeParameterIndex;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;


final class TypeArgumentVisitor extends AbstractTypeArgumentVisitor<
		TypeRefParameters,
		AccessDistributor> {

	static TypeRefParameters typeArguments(
			TypeInterpreter typeIp,
			TypeArgNode[] args,
			int index,
			AccessDistributor p,
			TypeConsumer consumer) {

		final TypeArgumentNode argumentNode = args[index].getArgument();

		if (argumentNode == null) {
			return null;
		}
		if (index == 0) {
			return argumentNode.accept(
					new TypeArgumentVisitor(typeIp, consumer, null),
					p);
		}

		final TypeRefParameters nestedArguments =
				typeArguments(typeIp, args, index - 1, p, consumer);

		if (nestedArguments == null) {
			return null;
		}

		return argumentNode.accept(
				new TypeArgumentVisitor(
						typeIp,
						consumer.noConsumption(),
						nestedArguments),
				p);

	}

	private final TypeInterpreter typeIp;
	private final TypeConsumer consumer;
	private final TypeRefParameters typeParameters;

	private TypeArgumentVisitor(
			TypeInterpreter typeIp,
			TypeConsumer consumer,
			TypeRefParameters typeParameters) {
		this.typeIp = typeIp;
		this.consumer = consumer;
		this.typeParameters = typeParameters;
	}

	public final Interpreter ip() {
		return typeIp().ip();
	}

	public final TypeInterpreter typeIp() {
		return this.typeIp;
	}

	@Override
	public TypeRefParameters visitAscendants(
			AscendantsNode ascendants,
			AccessDistributor p) {
		if (ascendants.hasSamples()) {
			return invalidTypeArgument(ascendants, p);
		}
		return singleTypeArgument(ascendants, p);
	}

	@Override
	public TypeRefParameters visitParentheses(
			ParenthesesNode parentheses,
			AccessDistributor p) {

		final SerialNode[] args = singleAlt(parentheses);

		if (args == null) {
			return invalidTypeArgument(parentheses, p);
		}
		if (args.length <= 1) {
			return typeArgumentInParenthesis(parentheses, p, args);
		}

		return typeArgumentsList(parentheses, p, args);
	}

	@Override
	protected TypeRefParameters visitRef(RefNode ref, AccessDistributor p) {
		return singleTypeArgument(ref, p);
	}

	@Override
	protected TypeRefParameters visitTypeArgument(
			TypeArgumentNode argument,
			AccessDistributor p) {
		return invalidTypeArgument(argument, p);
	}

	private TypeRefParameters invalidTypeArgument(
			Node argument,
			AccessDistributor p) {
		p.getContext().getLogger().error(
				"invalid_type_argument",
				argument,
				"Invalid type argument");
		return null;
	}

	private TypeRefParameters typeArgumentInParenthesis(
			ParenthesesNode parentheses,
			AccessDistributor p,
			SerialNode[] conjunction) {

		final ExpressionNode singleExpression =
				singleExpression(conjunction);

		if (singleExpression != null) {

			final TypeArgumentNode typeArgument =
					singleExpression.toTypeArgument();

			if (typeArgument != null) {
				return typeArgument.accept(this, p);
			}
		}

		return invalidTypeArgument(parentheses, p);
	}

	private TypeRefParameters typeArgumentsList(
			ParenthesesNode parentheses,
			AccessDistributor p,
			SerialNode[] args) {
		if (this.typeParameters != null) {
			redundantTypeArguments(
					p.getLogger(),
					this.typeParameters.getLocation());
		}

		final TypeRef[] params = new TypeRef[args.length];

		for (int i = 0; i < args.length; ++i) {

			final ParamTypeRef param = listTypeArgument(p, args, i);

			if (param == null) {
				return null;
			}

			params[i] = param.parameterize();
		}

		return new ArbitraryTypeRefParameters(
				location(p, parentheses),
				p.getScope(),
				params);
	}

	private ParamTypeRef listTypeArgument(
			AccessDistributor p,
			SerialNode[] args,
			int i) {

		final SerialNode serialNode = args[i];
		final StatementNode statement = serialNode.getStatement();

		if (statement == null) {
			invalidTypeArgument(args[i], p);
			return null;
		}

		final ExpressionNode expression = statement.toExpression();

		if (expression == null) {
			invalidTypeArgument(statement, p);
			return null;
		}

		return typeArgument(expression, p, i);
	}

	private TypeRefParameters singleTypeArgument(
			TypeArgumentNode node,
			AccessDistributor p) {

		final ParamTypeRef typeArgument = typeArgument(node, p, 0);

		return new ArbitraryTypeRefParameters(
				location(p, node),
				p.getScope(),
				typeArgument.parameterize());
	}

	private ParamTypeRef typeArgument(
			ExpressionNode node,
			AccessDistributor p,
			int index) {

		final Owner owner =
				node.accept(ip().refIp().ownerVisitor(), p.fromDeclaration());

		if (owner == null) {
			return null;
		}

		final TypeConsumer consumer =
				this.consumer.paramConsumer(new TypeParameterIndex(index));

		if (!owner.isMacroExpanding()) {
			return consumer.consumeType(
					owner.targetRef(),
					this.typeParameters);
		}

		return consumer.consumeType(
					expandMacro(owner.targetRef()),
					this.typeParameters);
	}

}
