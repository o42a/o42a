/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.compiler.ip.TypeVisitor.TYPE_VISITOR;
import static org.o42a.compiler.ip.member.FieldInterpreter.ADAPTER_FIELD_VISITOR;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.statement.*;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


final class FieldDeclarableVisitor
		extends AbstractDeclarableVisitor<FieldDeclaration, Distributor> {

	private static final VisibilityVisitor VISIBILITY_VISITOR =
		new VisibilityVisitor();

	private final CompilerContext context;
	private final DeclaratorNode declarator;

	FieldDeclarableVisitor(CompilerContext context, DeclaratorNode declarator) {
		this.context = context;
		this.declarator = declarator;
	}

	@Override
	public FieldDeclaration visitMemberRef(
			MemberRefNode memberNode,
			Distributor p) {

		final NameNode nameNode = memberNode.getName();

		if (nameNode == null) {
			p.getLogger().invalidDeclaration(memberNode);
			return null;
		}

		FieldDeclaration declaration = fieldDeclaration(
				location(p, memberNode),
				p,
				memberName(nameNode.getName()));

		declaration = setVisibility(declaration, memberNode);
		declaration = update(declaration, this.declarator);
		declaration = update(declaration, memberNode);

		return declaration;
	}

	@Override
	public FieldDeclaration visitDeclarableAdapter(
			DeclarableAdapterNode adapterNode,
			Distributor p) {
		if (p.getPlace().isImperative()) {
			p.getLogger().prohibitedLocalAdapter(adapterNode.getPrefix());
			return null;
		}

		final MemberRefNode memberNode = adapterNode.getMember();

		if (memberNode == null) {
			return null;
		}

		final Ref adapter =
			adapterNode.getMember().accept(ADAPTER_FIELD_VISITOR, p);

		if (adapter == null) {
			return null;
		}

		FieldDeclaration declaration = fieldDeclaration(
				location(p, adapterNode),
				p,
				adapterId(adapter.toStaticTypeRef()));

		declaration = update(declaration, this.declarator);
		declaration = update(declaration, memberNode);

		return declaration;
	}

	@Override
	protected FieldDeclaration visitDeclarable(
			DeclarableNode declarable,
			Distributor p) {
		this.context.getLogger().invalidDeclaration(declarable);
		return null;
	}

	static StaticTypeRef declaredIn(
			MemberRefNode memberRef,
			Distributor distributor) {

		final RefNode node = memberRef.getDeclaredIn();

		if (node == null) {
			return null;
		}

		final Ref declaredIn = node.accept(REF_VISITOR, distributor);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
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

		return owner.accept(VISIBILITY_VISITOR, declaration);
	}

	private FieldDeclaration update(
			FieldDeclaration declaration,
			DeclaratorNode declarator) {
		if (declaration == null) {
			return null;
		}

		final TypeNode node = declarator.getDefinitionType();

		if (node != null) {

			final TypeRef type =
				node.accept(TYPE_VISITOR, declaration.distribute());

			if (type != null) {
				declaration.setType(type);
			}
		}

		final DeclarationTarget target = declarator.getTarget();

		if (target.isOverride()) {
			declaration = declaration.override();
		}
		if (target.isAbstract()) {
			if (declaration.getScope().toLocal() != null) {
				declaration.getLogger().prohibitedLocalAbstract(
						declarator.getDefinitionAssignment(),
						declaration.getDisplayName());
				return null;
			}
			if (declaration.getVisibility() == Visibility.PRIVATE) {
				declaration.getLogger().prohibitedPrivateAbstract(
						declarator.getDefinitionAssignment(),
						declaration.getDisplayName());
				return null;
			}
			declaration = declaration.setAbstract();
		}
		if (target.isPrototype()) {
			declaration = declaration.prototype();
		}

		final DefinitionKind definitionKind = declarator.getDefinitionKind();

		if (definitionKind != null) {
			switch (definitionKind) {
			case VARIABLE:
				if (declaration.isPrototype()) {
					this.context.getLogger().prohibitedPrototype(
							declarator.getDefinitionAssignment());
					return null;
				}
				declaration = declaration.variable();
				break;
			case LINK:
				if (declaration.isPrototype()) {
					this.context.getLogger().prohibitedPrototype(
							declarator.getDefinitionAssignment());
					return null;
				}
				declaration = declaration.link();
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported definition kind: " + definitionKind);
			}
		}

		return declaration;
	}

	private FieldDeclaration update(
			FieldDeclaration declaration,
			MemberRefNode memberRef) {
		if (declaration == null) {
			return null;
		}

		final StaticTypeRef declaredIn =
			declaredIn(memberRef, declaration.distribute());

		if (declaredIn != null) {
			if (!declaration.isOverride()) {
				declaration.getLogger().prohibitedDeclaredIn(declaredIn);
			} else {
				declaration = declaration.setDeclaredIn(declaredIn);
			}
		}

		return declaration;
	}

	private static final class VisibilityVisitor
			extends AbstractExpressionVisitor<
					FieldDeclaration,
					FieldDeclaration> {

		@Override
		public FieldDeclaration visitScopeRef(
				ScopeRefNode ref,
				FieldDeclaration p) {
			if (p.getScope().toLocal() != null) {
				p.getLogger().prohibitedLocalVisibility(
						ref,
						p.getDisplayName());
				return p.setVisibility(Visibility.PRIVATE);
			}
			switch (ref.getType()) {
			case SELF:
				return p.setVisibility(Visibility.PRIVATE);
			case PARENT:
				return p.setVisibility(Visibility.PROTECTED);
			default:
				return super.visitScopeRef(ref, p);
			}
		}

		@Override
		protected FieldDeclaration visitExpression(
				ExpressionNode expression,
				FieldDeclaration p) {
			p.getLogger().illegalVisibility(expression);
			return null;
		}

	}

}
