/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.member.AdapterFieldVisitor.ADAPTER_FIELD_VISITOR;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.ast.type.TypeNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;


public final class FieldDeclarableVisitor
		extends AbstractDeclarableVisitor<FieldDeclaration, Distributor> {

	private static final VisibilityVisitor VISIBILITY_VISITOR =
			new VisibilityVisitor();

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
				fieldName(nameNode.getName()));

		declaration = setVisibility(declaration, memberNode);
		declaration = update(declaration, this.declarator);
		declaration = setDeclaredIn(declaration, memberNode);

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
		declaration = setDeclaredIn(declaration, memberNode);

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
			Interpreter ip,
			MemberRefNode memberRef,
			Distributor distributor) {

		final RefNode node = memberRef.getDeclaredIn();

		if (node == null) {
			return null;
		}

		final Ref declaredIn = node.accept(ip.refVisitor(), distributor);

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

		FieldDeclaration result;
		final TypeNode node = declarator.getDefinitionType();

		if (node == null) {
			result = declaration;
		} else {

			final TypeRef type = node.accept(
					this.ip.typeVisitor(),
					declaration.distribute());

			if (type != null) {
				result = declaration.setType(type);
			} else {
				result = declaration;
			}
		}

		final DeclarationTarget target = declarator.getTarget();

		if (target.isOverride()) {
			result = result.override();
		}
		if (target.isAbstract()) {
			if (declaration.getScope().toLocal() != null) {
				declaration.getLogger().prohibitedLocalAbstract(
						declarator.getDefinitionAssignment(),
						declaration.getDisplayName());
				return null;
			}
			if (!declaration.getVisibility().isOverridable()) {
				declaration.getLogger().prohibitedPrivateAbstract(
						declarator.getDefinitionAssignment(),
						declaration.getDisplayName());
				return null;
			}
			result = result.setAbstract();
		}
		if (target.isPrototype()) {
			result = result.prototype();
		}

		final DefinitionKind definitionKind = declarator.getDefinitionKind();

		if (definitionKind != null) {
			switch (definitionKind) {
			case VARIABLE:
				if (result.isPrototype()) {
					this.context.getLogger().prohibitedPrototype(
							declarator.getDefinitionAssignment());
					return null;
				}
				result = result.variable();
				break;
			case LINK:
				if (result.isPrototype()) {
					this.context.getLogger().prohibitedPrototype(
							declarator.getDefinitionAssignment());
					return null;
				}
				result = result.link();
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported definition kind: " + definitionKind);
			}
		}

		return result;
	}

	private FieldDeclaration setDeclaredIn(
			FieldDeclaration declaration,
			MemberRefNode memberRef) {
		if (declaration == null) {
			return null;
		}

		final StaticTypeRef declaredIn =
				declaredIn(this.ip, memberRef, declaration.distribute());

		if (declaredIn == null) {
			return declaration;
		}

		if (!declaration.isOverride()) {
			declaration.getLogger().prohibitedDeclaredIn(declaredIn);
			return declaration;
		}

		return declaration.setDeclaredIn(declaredIn);
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
