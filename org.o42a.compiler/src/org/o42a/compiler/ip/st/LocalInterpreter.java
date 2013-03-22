/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.st;

import static org.o42a.compiler.ip.file.OtherContextDistributor.distributeIn;
import static org.o42a.compiler.ip.ref.RefInterpreter.localName;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.compiler.ip.type.TypeInterpreter.definitionLinkType;
import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.Name;


public final class LocalInterpreter {

	public static Local local(
			Interpreter ip,
			CompilerContext context,
			Statements<?, ?> statements,
			DeclaratorNode declarator) {

		final Name name = declaredLocalName(
				declarator.getDeclarable(),
				context.getLogger());

		if (name == null) {
			return null;
		}
		if (declarator.getTarget() != DeclarationTarget.VALUE) {
			context.getLogger().error(
					"invalid_local_target",
					declarator.getDefinitionAssignment(),
					"A local can be declared only with `:=` sign");
		}

		final Ref ref = localRef(ip, context, statements, declarator);

		return statements.local(
				new Location(context, declarator.getDeclarable()),
				name,
				ref);
	}

	private static Name declaredLocalName(
			DeclarableNode declarable,
			CompilerLogger logger) {

		final MemberRefNode memberRef = declarable.toMemberRef();

		if (memberRef == null) {
			return null;
		}

		return localName(memberRef, logger);
	}

	private static Ref localRef(
			Interpreter ip,
			CompilerContext context,
			Statements<?, ?> statements,
			DeclaratorNode declarator) {

		final Distributor distributor =
				distributeIn(statements.nextDistributor(), context);
		final Ref value =
				localValue(ip, context, declarator, distributor);
		final InterfaceNode iface = declarator.getInterface();

		if (iface == null) {
			return value;
		}

		final PhraseBuilder phrase = new PhraseBuilder(
				ip,
				new Location(context, declarator.getDeclarable()),
				distributor,
				EXPRESSION_TYPE_CONSUMER);

		final LinkValueType linkType =
				definitionLinkType(iface.getKind().getType());

		phrase.setAncestor(linkType.typeRef(
				new Location(context, iface),
				statements.getScope()));

		final TypeNode type = declarator.getDefinitionType();

		if (type != null) {

			final TypeRefParameters typeParams =
					ip.typeIp().typeParameters(
							iface,
							distributor,
							phrase.typeConsumer());

			phrase.setTypeParameters(typeParams.toObjectTypeParameters());
		}

		phrase.argument(value);

		return phrase.toRef();
	}

	private static Ref localValue(
			Interpreter ip,
			CompilerContext context,
			DeclaratorNode declarator,
			Distributor distributor) {

		final ExpressionNode definition = declarator.getDefinition();

		if (definition == null) {
			return errorRef(
					new Location(context, declarator.getDeclarable()),
					distributor);
		}

		final Ref value = definition.accept(
					ip.targetExVisitor(),
					distributor);

		if (value != null) {
			return value;
		}

		return errorRef(new Location(context, definition), distributor);
	}

	private LocalInterpreter() {
	}

}
