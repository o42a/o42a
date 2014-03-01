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
package org.o42a.compiler.ip.st;

import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_NAME;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.statement.LocalNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Local;
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
			StatementsAccess statements,
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
					declarator.getTargetTypeNode(),
					"A local can be declared only with `:=` sign");
		}

		final ExpressionNode definition = declarator.getDefinition();

		if (definition == null) {
			return true;
		}

		final Ref ref = localValue(
				ip,
				context,
				declarator.getDeclarable(),
				definition,
				statements.nextDistributor().distributeIn(context));

		statements.get().local(
				new Location(context, declarator.getDeclarable()),
				name,
				ref);

		return true;
	}

	public static Local local(
			Interpreter ip,
			CompilerContext context,
			StatementsAccess statements,
			LocalNode local) {

		final LogInfo location;
		final Name name;

		if (local.getName() != null) {
			location = local.getName();
			name = local.getName().getName();
		} else {
			location = local.getSeparator();
			name = ANONYMOUS_LOCAL_NAME;
		}

		return local(
				ip,
				context,
				statements,
				location,
				name,
				local.getExpression());
	}

	public static Local local(
			Interpreter ip,
			CompilerContext context,
			StatementsAccess statements,
			final LogInfo location,
			final Name name,
			final ExpressionNode definition) {

		final Ref ref = localValue(
				ip,
				context,
				location,
				definition,
				statements.nextDistributor().distributeIn(context));

		return statements.get()
				.local(new Location(context, location), name, ref);
	}

	private static Name declaredLocalName(
			DeclarableNode declarable,
			StatementsAccess statements,
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
			StatementsAccess statements) {
		if (!isLocalRef(memberRef)) {
			if (statements.isDeclarative()) {
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

	private static Ref localValue(
			Interpreter ip,
			CompilerContext context,
			LogInfo location,
			ExpressionNode definition,
			AccessDistributor distributor) {
		if (definition == null) {
			return errorRef(new Location(context, location), distributor);
		}

		final Ref value = definition.accept(
				ip.expressionVisitor(),
				distributor);

		if (value != null) {
			return value;
		}

		return errorRef(new Location(context, definition), distributor);
	}

	private LocalInterpreter() {
	}

}
