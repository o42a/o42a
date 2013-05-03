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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.enclosingModuleRef;
import static org.o42a.compiler.ip.ref.RefInterpreter.isRootRef;
import static org.o42a.compiler.ip.st.LocalInterpreter.isLocalScopeRef;
import static org.o42a.compiler.ip.st.LocalInterpreter.localName;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberName.localName;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_MEMBER;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.util.string.Name;


final class OwnerVisitor
		extends AbstractExpressionVisitor<Owner, AccessDistributor> {

	static final Path MACROS_PATH = modulePath(CASE_INSENSITIVE.name("Macros"));

	private static final Name VOID_NAME = CASE_INSENSITIVE.name("Void");
	private static final Name FALSE_NAME = CASE_INSENSITIVE.name("False");
	private static final Name OBJECT_NAME = CASE_INSENSITIVE.name("Object");

	private final RefInterpreter ip;

	OwnerVisitor(RefInterpreter interpreter) {
		this.ip = interpreter;
	}

	public final RefInterpreter ip() {
		return this.ip;
	}

	@Override
	public final Owner visitScopeRef(ScopeRefNode ref, AccessDistributor p) {

		final ScopeType type = ref.getType();
		final Location location = location(p, ref);

		switch (type) {
		case IMPLIED:
			break;
		case SELF:

			final Ref selfRef =
					p.getAccessRules().selfRef(ip().ip(), location, p);

			if (selfRef == null) {
				return null;
			}

			return owner(p.getAccessRules(), selfRef);
		case PARENT:

			final Ref parentRef =
					p.getAccessRules().parentRef(location, p, null);

			if (parentRef == null) {
				return null;
			}

			return owner(p.getAccessRules(), parentRef);
		case MACROS:
			return owner(
					p.getAccessRules(),
					MACROS_PATH.bind(location, p.getScope()).target(p))
					.expandMacro(ref);
		case MODULE:
			return owner(
					p.getAccessRules(),
					enclosingModuleRef(location, p));
		case ROOT:
			return nonLinkOwner(
					p.getAccessRules(),
					ROOT_PATH.bind(location, p.getScope()).target(p));
		case LOCAL:
		case ANONYMOUS:
			return owner(
					p.getAccessRules(),
					new MemberById(
							location,
							p,
							ANONYMOUS_LOCAL_MEMBER,
							null).toRef());
		}

		p.getContext().getLogger().unresolvedScope(ref, type.getSign());

		return null;
	}

	@Override
	public final Owner visitParentRef(ParentRefNode ref, AccessDistributor p) {

		final Location location = location(p, ref);
		final Ref parentRef = p.getAccessRules().parentRef(
				location,
				p,
				ref.getName().getName());

		if (parentRef == null) {
			return null;
		}

		return owner(p.getAccessRules(), parentRef);
	}

	@Override
	public Owner visitMemberRef(MemberRefNode ref, AccessDistributor p) {
		if (ref.getMembership() == null) {

			final Name localName = localName(ref);

			if (OBJECT_NAME.is(localName)) {

				final Ref intrinsicObject = ip().intrinsicObject(ref, p);

				if (intrinsicObject != null) {
					return owner(p.getAccessRules(), intrinsicObject);
				}
			}
		}

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(memberRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitAdapterRef(AdapterRefNode ref, AccessDistributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(adapterRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitBodyRef(BodyRefNode ref, AccessDistributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(bodyRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitDeref(DerefNode ref, AccessDistributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(deref(ref, p, ownerVisitor));
	}

	@Override
	protected Owner visitRef(RefNode ref, AccessDistributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return nonLinkOwner(p.getAccessRules(), errorRef(location(p, ref), p));
	}

	@Override
	protected Owner visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {
		return owner(
				p.getAccessRules(),
				expression.accept(ip().ip().bodyExVisitor(), p));
	}

	final Owner owner(AccessRules accessRules, Ref ownerRef) {
		return ip().ownerFactory().owner(accessRules, ownerRef);
	}

	Owner memberRef(
			MemberRefNode ref,
			AccessDistributor p,
			MemberOwnerVisitor ownerVisitor) {

		final Owner owner;
		final ExpressionNode ownerNode = ref.getOwner();

		if (ownerNode != null) {
			if (isLocalScopeRef(ownerNode)) {
				return localRef(ref, p);
			}
			if (ref.getDeclaredIn() == null) {

				final NameNode nameNode = ref.getName();

				if (nameNode != null && isRootRef(ownerNode)) {

					final Name name = nameNode.getName();

					if (VOID_NAME.is(name)) {
						return nonLinkOwner(
								p.getAccessRules(),
								voidRef(location(p, ref), p));
					}
					if (FALSE_NAME.is(name)) {
						return nonLinkOwner(
								p.getAccessRules(),
								falseRef(location(p, ref), p));
					}
				}
			}

			owner = ownerNode.accept(ownerVisitor, p);
			if (owner == null) {
				return null;
			}
		} else {
			owner = null;
		}

		final StaticTypeRef declaredIn =
				ip().declaredIn(ref.getDeclaredIn(), p);

		if (owner != null) {

			final SignNode<Qualifier> qualifier = ref.getQualifier();
			final boolean macro =
					qualifier != null && qualifier.getType() == Qualifier.MACRO;
			final Owner memberOwner;

			if (!macro) {
				memberOwner = owner;
			} else {
				memberOwner = owner.plainOwner();
			}

			final Owner result = memberOwner.member(
					location(p, ref),
					ip().memberName(ref.getName().getName()),
					declaredIn);

			if (macro) {
				return result.expandMacro(ref.getQualifier());
			}

			return result;
		}

		return owner(
				p.getAccessRules(),
				new MemberById(
						location(p, ref.getName()),
						p,
						ip().memberName(ref.getName().getName()),
						declaredIn).toRef());
	}

	Owner adapterRef(
			AdapterRefNode ref,
			AccessDistributor p,
			MemberOwnerVisitor ownerVisitor) {

		final Owner owner = ref.getOwner().accept(ownerVisitor, p);

		if (owner == null) {
			return null;
		}

		final Ref type = ref.getType().accept(ip().adapterTypeVisitor(), p);

		if (type == null) {
			return null;
		}

		return owner.member(
				location(p, ref),
				adapterId(type.toStaticTypeRef()),
				ip().declaredIn(ref.getDeclaredIn(), p));
	}

	Owner bodyRef(
			BodyRefNode ref,
			AccessDistributor p,
			MemberOwnerVisitor ownerVisitor) {
		return ref.getOwner().accept(ownerVisitor, p);
	}

	Owner deref(
			DerefNode ref,
			AccessDistributor p,
			MemberOwnerVisitor ownerVisitor) {

		final Owner result = ref.getOwner().accept(ownerVisitor, p);

		if (result == null) {
			return null;
		}

		return result.deref(location(p, ref), location(p, ref.getSuffix()));
	}

	private Owner localRef(MemberRefNode ref, AccessDistributor p) {

		final NameNode nameNode = ref.getName();

		if (nameNode == null) {
			return null;
		}

		final StaticTypeRef declaredIn =
				ip().declaredIn(ref.getDeclaredIn(), p);

		return owner(
				p.getAccessRules(),
				new MemberById(
						location(p, ref),
						p,
						localName(nameNode.getName()),
						declaredIn).toRef());
	}

	private final Owner nonLinkOwner(AccessRules accessRules, Ref ownerRef) {
		return this.ip.ownerFactory().nonLinkOwner(accessRules, ownerRef);
	}

}
