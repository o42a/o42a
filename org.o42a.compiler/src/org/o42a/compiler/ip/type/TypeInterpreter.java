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
package org.o42a.compiler.ip.type;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.type.*;
import org.o42a.common.ref.ArbitraryTypeParameters;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.compiler.ip.type.ascendant.*;
import org.o42a.compiler.ip.type.param.TypeParameterIndex;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.core.value.link.LinkValueType;


public final class TypeInterpreter {

	private final Interpreter ip;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> ancestorVisitor;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			Distributor> staticAncestorVisitor;

	public TypeInterpreter(Interpreter ip) {
		this.ip = ip;
		this.ancestorVisitor = new AncestorVisitor(
				ip,
				null,
				TARGET_REFERRAL,
				NO_TYPE_CONSUMER);
		this.staticAncestorVisitor = new StaticAncestorVisitor(
				ip,
				null,
				TARGET_REFERRAL,
				NO_TYPE_CONSUMER);
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public static LinkValueType definitionLinkType(
			DefinitionKind definitionKind) {
		switch (definitionKind) {
		case LINK:
			return LinkValueType.LINK;
		case VARIABLE:
			return LinkValueType.VARIABLE;
		}
		throw new IllegalArgumentException(
				"Unknwon definition kind: " + definitionKind);
	}

	public final ArbitraryTypeParameters typeParameters(
			InterfaceNode ifaceNode,
			Distributor p,
			TypeConsumer consumer) {
		if (ifaceNode.getKind().getType() != DefinitionKind.LINK) {
			p.getLogger().error(
					"prohibited_type_mutability",
					ifaceNode.getKind(),
					"Mutability flag prohibited here. Use a single backquote");
		}

		final TypeParameterNode[] typeParamNodes = ifaceNode.getParameters();
		final TypeRef[] typeParams = new TypeRef[typeParamNodes.length];

		for (int i = 0; i < typeParams.length; ++i) {

			final TypeNode type = typeParamNodes[i].getType();

			if (type == null) {
				return null;
			}

			final TypeRef paramTypeRef = type.accept(
					typeVisitor(
							consumer.paramConsumer(new TypeParameterIndex(i))),
					p);

			if (paramTypeRef == null) {
				return null;
			}

			typeParams[i] = paramTypeRef;
		}

		return new ArbitraryTypeParameters(location(p, ifaceNode), typeParams);
	}

	public final TypeNodeVisitor<TypeRef, Distributor> typeVisitor(
			TypeConsumer consumer) {
		return new TypeVisitor(this, consumer);
	}

	public final ExpressionNodeVisitor<AncestorTypeRef, Distributor>
	ancestorVisitor(
			TypeParametersBuilder typeParameters,
			Referral referral,
			TypeConsumer typeConsumer) {
		if (typeParameters == null
				&& referral == TARGET_REFERRAL
				&& typeConsumer == NO_TYPE_CONSUMER) {
			return this.ancestorVisitor;
		}
		return new AncestorVisitor(
				ip(),
				typeParameters,
				referral,
				typeConsumer);
	}

	public final ExpressionNodeVisitor<AncestorTypeRef, Distributor>
	staticAncestorVisitor(
			TypeParametersBuilder typeParameters,
			Referral referral,
			TypeConsumer typeConsumer) {
		if (typeParameters == null
				&& referral == TARGET_REFERRAL
				&& typeConsumer == NO_TYPE_CONSUMER) {
			return this.staticAncestorVisitor;
		}
		return new StaticAncestorVisitor(
				ip(),
				typeParameters,
				referral,
				typeConsumer);
	}

	public AncestorTypeRef parseAncestor(
			AscendantsNode ascendantsNode,
			Distributor distributor) {

		final AscendantNode ancestorNode = ascendantsNode.getAncestor();

		return parseAncestor(
				distributor,
				ancestorNode,
				null,
				ascendantsNode.hasSamples() ? BODY_REFERRAL : TARGET_REFERRAL);
	}

	public AncestorTypeRef parseAncestor(
			Distributor distributor,
			AscendantNode ascendantNode,
			TypeParametersBuilder typeParameters,
			Referral referral) {

		final RefNodeVisitor<AncestorTypeRef, Distributor> ancestorVisitor;

		if (ascendantNode.getSeparator() == null) {
			ancestorVisitor = ancestorVisitor(
					typeParameters,
					referral,
					NO_TYPE_CONSUMER);
		} else {
			ancestorVisitor = staticAncestorVisitor(
					typeParameters,
					referral,
					NO_TYPE_CONSUMER);
		}

		final AncestorSpecVisitor specVisitor =
				new AncestorSpecVisitor(ancestorVisitor);

		return ascendantNode.getSpec().accept(specVisitor, distributor);
	}

	public AscendantsDefinition parseAscendants(
			AscendantsNode node,
			Distributor distributor) {

		final SampleSpecVisitor sampleSpecVisitor = new SampleSpecVisitor(ip());
		AscendantsDefinition ascendants = new AscendantsDefinition(
				location(distributor, node),
				distributor);
		final AncestorTypeRef ancestor = parseAncestor(node, distributor);

		if (!ancestor.isImplied()) {
			ascendants = ascendants.setAncestor(ancestor.getAncestor());
		}

		for (AscendantNode sampleNode : node.getSamples()) {

			final RefNode specNode = sampleNode.getSpec();

			if (specNode != null) {

				final StaticTypeRef sample =
						specNode.accept(sampleSpecVisitor, distributor);

				if (sample != null) {
					ascendants = ascendants.addSample(sample);
				}
			}
		}

		return ascendants;
	}

}
