/*
    Compiler
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.type.TypeInterpreter.invalidType;
import static org.o42a.compiler.ip.type.TypeInterpreter.redundantTypeArguments;

import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.core.ref.type.TypeRefParameters;


final class TypeVisitor
		extends AbstractTypeVisitor<ParamTypeRef, AccessDistributor> {

	private final TypeInterpreter typeIp;
	private final TypeConsumer consumer;
	private final TypeRefParameters typeParameters;

	TypeVisitor(TypeInterpreter ip, TypeConsumer consumer) {
		this.typeIp = ip;
		this.typeParameters = null;
		this.consumer = consumer;
	}

	private TypeVisitor(
			TypeInterpreter ip,
			TypeConsumer consumer,
			TypeRefParameters typeParameters) {
		this.typeIp = ip;
		this.typeParameters = typeParameters;
		this.consumer = consumer;
	}

	public final Interpreter ip() {
		return typeIp().ip();
	}

	public final TypeInterpreter typeIp() {
		return this.typeIp;
	}

	@Override
	public ParamTypeRef visitAscendants(
			AscendantsNode ascendants,
			AccessDistributor p) {
		if (ascendants.hasSamples()) {
			return super.visitAscendants(ascendants, p);
		}

		final AncestorTypeRef ancestor = typeIp().parseAncestor(
				p.fromDeclaration(),
				ascendants.getAncestor(),
				this.typeParameters);

		if (ancestor.isImplied()) {
			return super.visitAscendants(ascendants, p);
		}

		return ancestor.getAncestor();
	}

	@Override
	public ParamTypeRef visitTypeArguments(
			TypeArgumentsNode arguments,
			AccessDistributor p) {

		final TypeNode ascendantNode = arguments.getType();

		if (ascendantNode == null) {
			return super.visitTypeArguments(arguments, p);
		}

		final TypeRefParameters typeParameters;

		if (this.typeParameters != null) {
			redundantTypeArguments(p.getLogger(), arguments);
			typeParameters = this.typeParameters;
		} else {
			typeParameters = typeIp().typeArguments(
					arguments,
					p,
					this.consumer);
			if (typeParameters == null) {
				return null;
			}
		}

		return ascendantNode.accept(
				new TypeVisitor(
						typeIp(),
						this.consumer.noConsumption(),
						typeParameters),
				p);
	}

	@Override
	protected ParamTypeRef visitRef(RefNode node, AccessDistributor p) {

		final Owner ref =
				node.accept(ip().refIp().ownerVisitor(), p.fromDeclaration());

		if (ref == null) {
			return null;
		}

		if (!ref.isMacroExpanding()) {
			return this.consumer.consumeType(
					ref.targetRef(),
					this.typeParameters);
		}

		return this.consumer.consumeType(
				expandMacro(ref.targetRef()),
				this.typeParameters);
	}

	@Override
	protected ParamTypeRef visitType(TypeNode type, AccessDistributor p) {
		invalidType(p.getContext().getLogger(), type);
		return null;
	}

}
