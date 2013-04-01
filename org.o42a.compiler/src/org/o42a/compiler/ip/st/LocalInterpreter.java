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
import static org.o42a.compiler.ip.ref.AccessRules.ACCESS_FROM_DEFINITION;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;
import static org.o42a.compiler.ip.type.TypeInterpreter.definitionLinkType;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_NAME;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.ast.type.TypeParameterNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.compiler.ip.ref.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.log.LogInfo;
import org.o42a.util.string.Name;


public final class LocalInterpreter {

	public static boolean isLocalRef(MemberRefNode ref) {

		final ExpressionNode owner = ref.getOwner();

		if (owner == null) {
			return false;
		}

		return isLocalScopeRef(owner);
	}

	public static boolean isLocalScopeRef(ExpressionNode ref) {

		final RefNode ownerRef = ref.toRef();

		if (ownerRef == null) {
			return false;
		}

		final ScopeRefNode ownerScope = ownerRef.toScopeRef();

		return ownerScope != null && ownerScope.getType() == ScopeType.LOCAL;
	}

	public static Name localName(MemberRefNode ref) {
		if (!isLocalRef(ref)) {
			return null;
		}

		final NameNode name = ref.getName();

		if (name == null) {
			return null;
		}

		return name.getName();
	}

	public static boolean local(
			Interpreter ip,
			CompilerContext context,
			Statements<?, ?> statements,
			DeclaratorNode declarator) {

		final Name name = declaredLocalName(
				declarator.getDeclarable(),
				statements,
				context.getLogger());

		if (name == null) {
			return false;
		}
		if (declarator.getTarget() != DeclarationTarget.VALUE) {
			context.getLogger().error(
					"invalid_local_target",
					declarator.getDefinitionAssignment(),
					"A local can be declared only with `:=` sign");
		}

		final Ref ref = localRef(
				ip,
				context,
				statements,
				declarator.getDeclarable(),
				declarator.getInterface(),
				declarator.getDefinition(),
				TARGET_REFERRAL);

		statements.local(
				new Location(context, declarator.getDeclarable()),
				name,
				ref);

		return true;
	}

	public static Local local(
			Interpreter ip,
			CompilerContext context,
			Statements<?, ?> statements,
			InterfaceNode iface,
			LocalNode local,
			Referral referral) {

		final LogInfo location;
		final Name name;

		if (local.getName() != null) {
			location = local.getName();
			name = local.getName().getName();
		} else {
			location = local.getSeparator();
			name = ANONYMOUS_LOCAL_NAME;
		}

		final Ref ref = localRef(
				ip,
				context,
				statements,
				location,
				iface,
				local.getExpression(),
				referral);

		return statements.local(new Location(context, location), name, ref);
	}

	private static Name declaredLocalName(
			DeclarableNode declarable,
			Statements<?, ?> statements,
			CompilerLogger logger) {

		final MemberRefNode memberRef = declarable.toMemberRef();

		if (memberRef == null) {
			return null;
		}

		final Name localName = extractLocalName(memberRef, statements);

		if (localName == null) {
			return null;
		}
		if (memberRef.getMembership() != null) {
			logger.prohibitedDeclaredIn(memberRef.getMembership());
		}

		return localName;
	}

	private static Name extractLocalName(
			MemberRefNode memberRef,
			Statements<?, ?> statements) {
		if (!isLocalRef(memberRef)) {
			if (statements.getSentenceFactory().isDeclarative()) {
				return null;
			}
			if (memberRef.getOwner() != null) {
				return null;
			}
		}

		final NameNode nameNode = memberRef.getName();

		if (nameNode == null) {
			return null;
		}

		return nameNode.getName();
	}

	private static Ref localRef(
			Interpreter ip,
			CompilerContext context,
			Statements<?, ?> statements,
			LogInfo location,
			InterfaceNode iface,
			ExpressionNode definition,
			Referral referral) {

		final AccessDistributor distributor =
				ACCESS_FROM_DEFINITION.distribute(
						distributeIn(statements.nextDistributor(), context));
		final Ref value = localValue(
				ip,
				context,
				location,
				definition,
				distributor,
				referral);

		if (iface == null) {
			return value;
		}

		final PhraseBuilder phrase = new PhraseBuilder(
				ip,
				new Location(context, location),
				distributor,
				EXPRESSION_TYPE_CONSUMER);
		final LinkValueType linkType =
				definitionLinkType(iface.getKind().getType());

		phrase.referBody();
		phrase.setAncestor(linkType.typeRef(
				new Location(context, iface),
				statements.getScope()));

		final TypeNode type = interfaceType(iface);

		if (type != null) {

			final TypeRefParameters typeParams =
					ip.typeIp().typeParameters(
							iface,
							distributor,
							phrase.typeConsumer());

			phrase.setTypeParameters(typeParams.toObjectTypeParameters());
		} else {
			phrase.setTypeParameters(
					linkType.typeParameters(value.getInterface())
					.toObjectTypeParameters());
		}

		phrase.argument(value);

		return phrase.toRef();
	}

	private static Ref localValue(
			Interpreter ip,
			CompilerContext context,
			LogInfo location,
			ExpressionNode definition,
			AccessDistributor distributor,
			Referral referral) {
		if (definition == null) {
			return errorRef(new Location(context, location), distributor);
		}

		final Ref value = definition.accept(
				referral.expressionVisitor(ip, NO_TYPE_CONSUMER),
				distributor);

		if (value != null) {
			return value;
		}

		return errorRef(new Location(context, definition), distributor);
	}

	private static TypeNode interfaceType(InterfaceNode iface) {

		final TypeParameterNode[] parameters = iface.getParameters();

		if (parameters.length == 0) {
			return null;
		}

		return parameters[0].getType();
	}

	private LocalInterpreter() {
	}

}
