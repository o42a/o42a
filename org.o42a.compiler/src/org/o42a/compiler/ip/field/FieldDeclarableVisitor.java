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
package org.o42a.compiler.ip.field;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.ADAPTER_FIELD_REF_IP;
import static org.o42a.compiler.ip.type.TypeInterpreter.definitionLinkType;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.field.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.ast.type.TypeNode;
import org.o42a.common.macro.type.TypeParameterMemberKey;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.core.member.Visibility;
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
		declaration = update(declaration, p, this.declarator);
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
				adapterId(adapterId.toStaticTypeRef()));

		declaration = update(declaration, p, this.declarator);
		declaration = setDeclaredIn(declaration, p, memberNode);

		return declaration;
	}

	@Override
	public FieldDeclaration visitMacroExpansion(
			MacroExpansionNode expansion,
			AccessDistributor p) {

		final ExpressionNode operand = expansion.getOperand();

		if (operand == null) {
			return null;
		}

		final FieldDeclaration declaration =
				operand.accept(new MacroNameVisitor(), p);

		if (declaration == null) {
			return null;
		}
		if (declaration.isPrototype()) {
			getLogger().error(
					"porhibited_macro_prototype",
					this.declarator.getDefinitionAssignment(),
					"Macro can not be declared as prototype");
		}
		if (declaration.getLinkType() != null) {
			getLogger().error(
					"porhibited_macro_link",
					this.declarator.getInterface(),
					"Macro does not have an interface");
		}

		return declaration.macro();
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
			return declaration.setVisibility(Visibility.PRIVATE);
		case PARENT:
			return declaration.setVisibility(Visibility.PROTECTED);
		case MODULE:
		case ROOT:
		case LOCAL:
		case ANONYMOUS:
		case MACROS:
		case IMPLIED:
		}

		return illegalVisibility(declaration, member);
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
			AccessDistributor distributor,
			DeclaratorNode declarator) {
		if (declaration == null) {
			return null;
		}

		FieldDeclaration result = declaration;
		final DefinitionKind definitionKind = declarator.getDefinitionKind();

		if (definitionKind != null) {
			result = result.setLinkType(definitionLinkType(definitionKind));

			final TypeNode typeNode = declarator.getDefinitionType();

			if (typeNode != null) {

				final TypeParameterMemberKey parameterKey =
						new TypeParameterMemberKey(
								result.getLinkType().interfaceKey(
										declaration.getContext()
										.getIntrinsics()));
				final ParamTypeRef type = typeNode.accept(
						ip().typeIp().typeVisitor(
								new FieldNesting(result)
								.toTypeConsumer()
								.paramConsumer(parameterKey)),
								distributor);

				if (type != null) {
					result = result.setType(type.parameterize());
				}
			}
		}

		final DeclarationTarget target = declarator.getTarget();

		if (target.isOverride()) {
			result = result.override();
		}
		if (target.isAbstract()) {
			if (!declaration.getVisibility().isOverridable()) {
				declaration.getLogger().prohibitedPrivateAbstract(
						declarator.getDefinitionAssignment(),
						declaration.getDisplayName());
				return null;
			}
			result = result.setAbstract();
		}
		if (target.isPrototype()) {
			if (definitionKind != null) {
				getLogger().error(
						"prohibited_link_prototype",
						declarator.getDefinitionAssignment(),
						"Field can not be declared as prototype");
			} else if (declaration.isAdapter()) {
				getLogger().error(
						"prohibited_adapter_prototype",
						declarator.getDefinitionAssignment(),
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

		if (!declaration.isOverride()) {
			declaration.getLogger().prohibitedDeclaredIn(
					declaredIn.getLocation());
			return declaration;
		}

		return declaration.setDeclaredIn(declaredIn);
	}

	private final class MacroNameVisitor
			extends AbstractExpressionVisitor<FieldDeclaration, AccessDistributor> {

		@Override
		public FieldDeclaration visitMemberRef(
				MemberRefNode ref,
				AccessDistributor p) {
			return ref.accept(FieldDeclarableVisitor.this, p);
		}

		@Override
		protected FieldDeclaration visitExpression(
				ExpressionNode expression,
				AccessDistributor p) {
			getLogger().invalidDeclaration(expression);
			return null;
		}

	}

}
