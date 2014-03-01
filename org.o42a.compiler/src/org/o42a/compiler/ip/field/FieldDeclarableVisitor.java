/*
    Compiler
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.field;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.ADAPTER_FIELD_REF_IP;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.member.field.VisibilityMode.AUTO_VISIBILITY;
import static org.o42a.core.member.field.VisibilityMode.PRIVATE_VISIBILITY;
import static org.o42a.core.member.field.VisibilityMode.PROTECTED_VISIBILITY;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public final class FieldDeclarableVisitor
		extends AbstractDeclarableVisitor<FieldDeclaration, AccessDistributor> {

	private final Interpreter ip;
	private final CompilerContext context;
	private final DeclaratorNode declarator;

	public FieldDeclarableVisitor(
			Interpreter ip,
			CompilerContext context,
			DeclaratorNode declarator) {
		this.ip = ip;
		this.context = context;
		this.declarator = declarator;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public FieldDeclaration visitMemberRef(
			MemberRefNode memberNode,
			AccessDistributor p) {

		final NameNode nameNode = memberNode.getName();

		if (nameNode == null) {
			p.getLogger().invalidDeclaration(memberNode);
			return null;
		}


		FieldDeclaration declaration = fieldDeclaration(
				location(p, memberNode),
				p,
				fieldName(nameNode.getName()));

		declaration = setVisibility(declaration, memberNode);
		declaration = update(declaration, this.declarator);
		declaration = setDeclaredIn(declaration, p, memberNode);

		return declaration;
	}

	@Override
	public FieldDeclaration visitDeclarableAdapter(
			DeclarableAdapterNode adapterNode,
			AccessDistributor p) {

		final MemberRefNode memberNode = adapterNode.getMember();

		if (memberNode == null) {
			return null;
		}

		final Ref adapterId = adapterNode.getMember().accept(
				ADAPTER_FIELD_REF_IP.refVisitor(),
				p.fromDeclaration());

		if (adapterId == null) {
			return null;
		}

		FieldDeclaration declaration = fieldDeclaration(
				location(p, adapterNode),
				p,
				adapterId(adapterId.toTypeRef()));

		declaration = update(declaration, this.declarator);
		declaration = setDeclaredIn(declaration, p, memberNode);

		return declaration;
	}

	@Override
	protected FieldDeclaration visitDeclarable(
			DeclarableNode declarable,
			AccessDistributor p) {
		getLogger().invalidDeclaration(declarable);
		return null;
	}

	private static StaticTypeRef declaredIn(
			Interpreter ip,
			MemberRefNode memberRef,
			AccessDistributor distributor) {

		final RefNode node = memberRef.getDeclaredIn();

		if (node == null) {
			return null;
		}

		final Ref declaredIn = node.accept(
				ip.refVisitor(),
				distributor.fromDeclaration());

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	private CompilerLogger getLogger() {
		return this.context.getLogger();
	}

	private FieldDeclaration setVisibility(
			FieldDeclaration declaration,
			MemberRefNode member) {
		if (declaration == null) {
			return null;
		}

		final ExpressionNode owner = member.getOwner();

		if (owner == null) {
			return declaration;
		}

		final RefNode ref = owner.toRef();

		if (ref == null) {
			return illegalVisibility(declaration, member);
		}

		final ScopeRefNode scopeRef = ref.toScopeRef();

		if (scopeRef == null) {
			return illegalVisibility(declaration, member);
		}

		switch (scopeRef.getType()) {
		case SELF:
			return declaration.setVisibilityMode(PRIVATE_VISIBILITY);
		case PARENT:
			return declaration.setVisibilityMode(PROTECTED_VISIBILITY);
		case MACRO:
			return declareMacro(declaration);
		case IMPLIED:
			if (!declaration.getDeclarationMode().canOverride()) {
				break;
			}
			return declaration.setVisibilityMode(AUTO_VISIBILITY);
		case MODULE:
		case ROOT:
		case LOCAL:
		case ANONYMOUS:
		case MACROS:
		}

		return illegalVisibility(declaration, member);
	}

	private FieldDeclaration declareMacro(FieldDeclaration declaration) {
		if (declaration.getPrototypeMode().isPrototype()) {
			getLogger().error(
					"porhibited_macro_prototype",
					this.declarator.getTargetTypeNode(),
					"Macro can not be declared as prototype");
		}
		return declaration.macro();
	}

	private FieldDeclaration illegalVisibility(
			FieldDeclaration declaration,
			LogInfo location) {
		declaration.getLogger().error(
				"illegal_visibility",
				location,
				"Illegal field visibility qualifier");
		return declaration;
	}

	private FieldDeclaration update(
			FieldDeclaration declaration,
			DeclaratorNode declarator) {
		if (declaration == null) {
			return null;
		}

		FieldDeclaration result = declaration;
		final DeclarationTarget target = declarator.getTarget();

		if (target == null) {
			if (declaration.isAdapter()) {
				result = result.overrideOrDeclare();
			} else {
				result = result.override();
			}
		} else if (target.isOverride()) {
			result = result.override();
		} else if (target.isStatic()) {
			if (declaration.isAdapter()) {
				declaration.getLogger().error(
						"prohibited_static_adapter",
						declarator.getTargetTypeNode(),
						"Adapter can not be static");
			} else {
				result = result.makeStatic();
			}
		}
		if (target != null && target.isAbstract()) {
			if (!declaration.getVisibilityMode().isOverridable()) {
				declaration.getLogger().error(
						"prohibited_private_abstract",
						declarator.getTargetTypeNode(),
						"Private field '%s' can not be abstract",
						declaration.getDisplayName());
				return null;
			}
			result = result.makeAbstract();
		}
		if (target != null && target.isPrototype()) {
			if (declaration.isAdapter()) {
				getLogger().error(
						"prohibited_adapter_prototype",
						declarator.getTargetTypeNode(),
						"Adapter can not be declared as prototype");
			} else {
				result = result.prototype();
			}
		}

		return result;
	}

	private FieldDeclaration setDeclaredIn(
			FieldDeclaration declaration,
			AccessDistributor distributor,
			MemberRefNode memberRef) {
		if (declaration == null) {
			return null;
		}

		final StaticTypeRef declaredIn =
				declaredIn(this.ip, memberRef, distributor);

		if (declaredIn == null) {
			return declaration;
		}

		if (!declaration.getDeclarationMode().canOverride()) {
			declaration.getLogger().prohibitedDeclaredIn(
					declaredIn.getLocation());
			return declaration;
		}

		return declaration.setDeclaredIn(declaredIn);
	}

}
