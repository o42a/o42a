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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.enclosingModulePath;
import static org.o42a.compiler.ip.ref.RefInterpreter.isRootRef;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.util.string.Name;


final class OwnerVisitor extends AbstractExpressionVisitor<Owner, Distributor> {

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
	public final Owner visitScopeRef(ScopeRefNode ref, Distributor p) {

		final ScopeType type = ref.getType();
		final Location location = location(p, ref);

		switch (type) {
		case IMPLIED:
			break;
		case SELF:
			return owner(
					SELF_PATH.bind(location, p.getScope()).target(p));
		case PARENT:
			return owner(
					ip().parentPath(location, null, p.getContainer())
					.bind(location, p.getScope())
					.target(p));
		case MACROS:
			return owner(MACROS_PATH.bind(location, p.getScope()).target(p))
					.expandMacro(ref);
		case MODULE:
			return nonLinkOwner(
					enclosingModulePath(p.getContainer())
					.bind(location, p.getScope())
					.target(p));
		case ROOT:
			return nonLinkOwner(
					ROOT_PATH.bind(location, p.getScope()).target(p));
		}

		p.getContext().getLogger().unresolvedScope(ref, type.getSign());

		return null;
	}

	@Override
	public final Owner visitParentRef(ParentRefNode ref, Distributor p) {

		final Location location = location(p, ref);
		final Path parentPath = ip().parentPath(
				location,
				ref.getName().getName(),
				p.getContainer());

		return owner(parentPath.bind(location, p.getScope()).target(p));
	}

	@Override
	public final Owner visitIntrinsicRef(IntrinsicRefNode ref, Distributor p) {
		if (OBJECT_NAME.is(ref.getName().getName())) {
			return owner(ip().objectIntrinsic(ref, p));
		}
		return super.visitIntrinsicRef(ref, p);
	}

	@Override
	public Owner visitMemberRef(MemberRefNode ref, Distributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(memberRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitAdapterRef(AdapterRefNode ref, Distributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(adapterRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitBodyRef(BodyRefNode ref, Distributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(bodyRef(ref, p, ownerVisitor));
	}

	@Override
	public Owner visitDeref(DerefNode ref, Distributor p) {

		final MemberOwnerVisitor ownerVisitor = new MemberOwnerVisitor(this);

		return ownerVisitor.expandMacro(deref(ref, p, ownerVisitor));
	}

	@Override
	protected Owner visitRef(RefNode ref, Distributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return nonLinkOwner(errorRef(location(p, ref), p));
	}

	@Override
	protected Owner visitExpression(
			ExpressionNode expression,
			Distributor p) {
		return owner(expression.accept(ip().ip().bodyExVisitor(), p));
	}

	final Owner owner(Ref ownerRef) {
		return ip().ownerFactory().owner(ownerRef);
	}

	Owner memberRef(
			MemberRefNode ref,
			Distributor p,
			MemberOwnerVisitor ownerVisitor) {

		final RefNode declaredInNode = ref.getDeclaredIn();
		final Owner owner;
		final ExpressionNode ownerNode = ref.getOwner();

		if (ownerNode != null) {
			if (declaredInNode == null) {

				final NameNode nameNode = ref.getName();

				if (nameNode != null && isRootRef(ownerNode)) {

					final Name name = nameNode.getName();

					if (VOID_NAME.is(name)) {
						return nonLinkOwner(voidRef(location(p, ref), p));
					}
					if (FALSE_NAME.is(name)) {
						return nonLinkOwner(falseRef(location(p, ref), p));
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

		final StaticTypeRef declaredIn = ip().declaredIn(declaredInNode, p);

		if (owner != null) {

			final Owner result = owner.member(
					location(p, ref),
					ip().memberName(ref.getName().getName()),
					declaredIn);
			final SignNode<Qualifier> qualifier = ref.getQualifier();

			if (qualifier != null && qualifier.getType() == Qualifier.MACRO) {
				return result.expandMacro(ref.getQualifier());
			}

			return result;
		}

		return owner(new MemberById(
				ip().ip(),
				location(p, ref.getName()),
				p,
				ip().memberName(ref.getName().getName()),
				declaredIn).toRef());
	}

	Owner adapterRef(
			AdapterRefNode ref,
			Distributor p,
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
			Distributor p,
			MemberOwnerVisitor ownerVisitor) {

		final Owner result = ref.getOwner().accept(ownerVisitor, p);

		if (result == null) {
			return null;
		}

		return result.body(location(p, ref), location(p, ref.getSuffix()));
	}

	Owner deref(
			DerefNode ref,
			Distributor p,
			MemberOwnerVisitor ownerVisitor) {

		final Owner result = ref.getOwner().accept(ownerVisitor, p);

		if (result == null) {
			return null;
		}

		return result.deref(location(p, ref), location(p, ref.getSuffix()));
	}

	private final Owner nonLinkOwner(Ref ownerRef) {
		return this.ip.ownerFactory().nonLinkOwner(ownerRef);
	}

}
