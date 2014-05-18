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
package org.o42a.compiler.ip.type;

import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.type.TypeArgNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.ancestor.AncestorTypeRef;
import org.o42a.compiler.ip.type.ancestor.AncestorVisitor;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public final class TypeInterpreter {

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

	public TypeInterpreter(Interpreter ip) {
		this.ip = ip;
		this.ancestorVisitor = new AncestorVisitor(
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

}
