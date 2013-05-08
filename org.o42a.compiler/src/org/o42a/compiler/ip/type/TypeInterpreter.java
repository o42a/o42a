/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.ascendant.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public final class TypeInterpreter {

	public static void invalidType(CompilerLogger logger, LogInfo location) {
		logger.error("invalid_type", location, "Not a valid type reference");
	}

	public static void redundantTypeArguments(
			CompilerLogger logger,
			LogInfo location) {
		logger.error(
				"redundant_type_arguments",
				location,
				"Redundant type arguments");
	}

	private final Interpreter ip;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			AccessDistributor> ancestorVisitor;
	private final ExpressionNodeVisitor<
			AncestorTypeRef,
			AccessDistributor> staticAncestorVisitor;

	public TypeInterpreter(Interpreter ip) {
		this.ip = ip;
		this.ancestorVisitor = new AncestorVisitor(
				ip,
				null,
				NO_TYPE_CONSUMER);
		this.staticAncestorVisitor = new StaticAncestorVisitor(
				ip,
				null,
				NO_TYPE_CONSUMER);
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public TypeRefParameters typeArguments(
			TypeArgumentsNode node,
			AccessDistributor p,
			TypeConsumer consumer) {

		final TypeArgNode[] args = node.getArguments();

		return TypeArgumentVisitor.typeArguments(
				this,
				args,
				args.length - 1,
				p,
				consumer);
	}

	public final TypeNodeVisitor<ParamTypeRef, AccessDistributor> typeVisitor(
			TypeConsumer consumer) {
		return new TypeVisitor(this, consumer);
	}

	public final ExpressionNodeVisitor<AncestorTypeRef, AccessDistributor>
	ancestorVisitor(
			TypeRefParameters typeParameters,
			TypeConsumer typeConsumer) {
		if (typeParameters == null
				&& typeConsumer == NO_TYPE_CONSUMER) {
			return this.ancestorVisitor;
		}
		return new AncestorVisitor(
				ip(),
				typeParameters,
				typeConsumer);
	}

	public final ExpressionNodeVisitor<AncestorTypeRef, AccessDistributor>
	staticAncestorVisitor(
			TypeRefParameters typeParameters,
			TypeConsumer typeConsumer) {
		if (typeParameters == null
				&& typeConsumer == NO_TYPE_CONSUMER) {
			return this.staticAncestorVisitor;
		}
		return new StaticAncestorVisitor(
				ip(),
				typeParameters,
				typeConsumer);
	}

	public AncestorTypeRef parseAncestor(
			AscendantsNode ascendantsNode,
			AccessDistributor distributor) {

		final AscendantNode ancestorNode = ascendantsNode.getAncestor();

		return parseAncestor(
				distributor,
				ancestorNode,
				null);
	}

	public AncestorTypeRef parseAncestor(
			AccessDistributor distributor,
			AscendantNode ascendantNode,
			TypeRefParameters typeParameters) {

		final
		RefNodeVisitor<AncestorTypeRef, AccessDistributor> ancestorVisitor;

		if (ascendantNode.getSeparator() == null) {
			ancestorVisitor = ancestorVisitor(
					typeParameters,
					NO_TYPE_CONSUMER);
		} else {
			ancestorVisitor = staticAncestorVisitor(
					typeParameters,
					NO_TYPE_CONSUMER);
		}

		final AncestorSpecVisitor specVisitor =
				new AncestorSpecVisitor(ancestorVisitor);

		return ascendantNode.getSpec().accept(specVisitor, distributor);
	}

	public AscendantsDefinition parseAscendants(
			AscendantsNode node,
			AccessDistributor distributor) {

		final SampleSpecVisitor sampleSpecVisitor = new SampleSpecVisitor(ip());
		AscendantsDefinition ascendants = new AscendantsDefinition(
				location(distributor, node),
				distributor);
		final AncestorTypeRef ancestor = parseAncestor(node, distributor);

		if (!ancestor.isImplied()) {
			ascendants = ancestor.applyTo(ascendants);
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
