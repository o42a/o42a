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

import static org.o42a.compiler.ip.AncestorSpecVisitor.parseAncestor;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.AncestorTypeRef;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


public final class TypeVisitor
		extends AbstractTypeVisitor<TypeRef, Distributor> {

	private final Interpreter ip;
	private final TypeConsumer consumer;
	private final ValueStructFinder valueStruct;

	public TypeVisitor(Interpreter ip, TypeConsumer consumer) {
		this.ip = ip;
		this.valueStruct = null;
		this.consumer = consumer;
	}

	private TypeVisitor(
			Interpreter ip,
			TypeConsumer consumer,
			ValueStructFinder valueStruct) {
		this.ip = ip;
		this.valueStruct = valueStruct;
		this.consumer = consumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef visitAscendants(AscendantsNode ascendants, Distributor p) {
		if (ascendants.hasSamples()) {
			return super.visitAscendants(ascendants, p);
		}

		final AncestorTypeRef ancestor = parseAncestor(
				ip(),
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
	public TypeRef visitValueType(ValueTypeNode valueType, Distributor p) {

		final TypeNode ascendantNode = valueType.getAscendant();

		if (ascendantNode == null) {
			return super.visitValueType(valueType, p);
		}

		final ValueStructFinder vsFinder;
		final InterfaceNode ifaceNode = valueType.getValueType();

		if (this.valueStruct != null) {
			p.getLogger().error(
					"redundant_value_type",
					ifaceNode,
					"Redundant value type");
			vsFinder = this.valueStruct;
		} else {
			vsFinder = ip().typeParameters(
					ifaceNode,
					p,
					this.consumer.paramConsumer());
			if (vsFinder == null) {
				return null;
			}
		}

		return ascendantNode.accept(
				new TypeVisitor(ip(), NO_TYPE_CONSUMER, vsFinder),
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
				macroRef.expandMacro(),
				this.valueStruct);
	}

	@Override
	protected TypeRef visitRef(RefNode node, Distributor p) {

		final Ref ref = node.accept(ip().bodyRefVisitor(), p);

		if (ref == null) {
			return null;
		}

		return this.consumer.consumeType(ref, this.valueStruct);
	}

	@Override
	protected TypeRef visitType(Node type, Distributor p) {
		p.getContext().getLogger().invalidType(type);
		return null;
	}

}
