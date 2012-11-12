/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.type.macro.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.compiler.ip.type.macro.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


public final class TypeVisitor
		extends AbstractTypeVisitor<TypeRef, Distributor> {

	private final TypeInterpreter typeIp;
	private final TypeConsumer consumer;
	private final ValueStructFinder valueStruct;

	public TypeVisitor(TypeInterpreter ip, TypeConsumer consumer) {
		this.typeIp = ip;
		this.valueStruct = null;
		this.consumer = consumer;
	}

	private TypeVisitor(
			TypeInterpreter ip,
			TypeConsumer consumer,
			ValueStructFinder valueStruct) {
		this.typeIp = ip;
		this.valueStruct = valueStruct;
		this.consumer = consumer;
	}

	public final Interpreter ip() {
		return typeIp().ip();
	}

	public final TypeInterpreter typeIp() {
		return this.typeIp;
	}

	@Override
	public TypeRef visitAscendants(AscendantsNode ascendants, Distributor p) {
		if (ascendants.hasSamples()) {
			return super.visitAscendants(ascendants, p);
		}

		final AncestorTypeRef ancestor = typeIp().parseAncestor(
				p,
				ascendants.getAncestor(),
				this.valueStruct,
				BODY_REFERRAL);

		if (ancestor.isImplied()) {
			return super.visitAscendants(ascendants, p);
		}

		return ancestor.getAncestor();
	}

	@Override
	public TypeRef visitTypeParameters(
			TypeParametersNode parameters,
			Distributor p) {

		final TypeNode ascendantNode = parameters.getType();

		if (ascendantNode == null) {
			return super.visitTypeParameters(parameters, p);
		}

		final ValueStructFinder vsFinder;
		final InterfaceNode ifaceNode = parameters.getParameters();

		if (this.valueStruct != null) {
			p.getLogger().error(
					"redundant_value_type",
					ifaceNode,
					"Redundant value type");
			vsFinder = this.valueStruct;
		} else {
			vsFinder = typeIp().typeParameters(
					ifaceNode,
					p,
					this.consumer);
			if (vsFinder == null) {
				return null;
			}
		}

		return ascendantNode.accept(
				new TypeVisitor(typeIp(), NO_TYPE_CONSUMER, vsFinder),
				p);
	}

	@Override
	public TypeRef visitMacroExpansion(
			MacroExpansionNode expansion,
			Distributor p) {

		final ExpressionNode operandNode = expansion.getOperand();

		if (operandNode == null) {
			return null;
		}

		final Ref macroRef = operandNode.accept(ip().bodyExVisitor(), p);

		if (macroRef == null) {
			return null;
		}

		return this.consumer.consumeType(
				expandMacro(macroRef),
				this.valueStruct);
	}

	@Override
	public TypeRef visitTypeExpression(TypeExpressionNode type, Distributor p) {

		final Ref ref = type.getExpression().accept(
				ip().bodyExVisitor(this.consumer),
				p);

		if (ref == null) {
			return null;
		}

		return this.consumer.consumeType(ref, this.valueStruct);
	}

	@Override
	protected TypeRef visitRef(RefNode node, Distributor p) {

		final Owner ref = node.accept(ip().refIp().ownerVisitor(), p);

		if (ref == null) {
			return null;
		}

		if (!ref.isMacroExpanding()) {
			return this.consumer.consumeType(ref.bodyRef(), this.valueStruct);
		}

		return this.consumer.consumeType(
				expandMacro(ref.bodyRef()),
				this.valueStruct);
	}

	@Override
	protected TypeRef visitType(Node type, Distributor p) {
		p.getContext().getLogger().invalidType(type);
		return null;
	}

}
