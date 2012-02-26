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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.enclosingModulePath;
import static org.o42a.compiler.ip.ref.RefInterpreter.isRootRef;
import static org.o42a.compiler.ip.ref.RefInterpreter.parentPath;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.ref.MemberById;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;


final class OwnerVisitor extends AbstractExpressionVisitor<Owner, Distributor> {

	private final RefVisitor refVisitor;

	OwnerVisitor(RefVisitor refVisitor) {
		this.refVisitor = refVisitor;
	}

	public final Interpreter ip() {
		return this.refVisitor.ip();
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
					parentPath(ip(), location, null, p.getContainer())
					.bind(location, p.getScope())
					.target(p));
		case MODULE:
			return bodyOwner(
					enclosingModulePath(p.getContainer())
					.bind(location, p.getScope())
					.target(p));
		case ROOT:
			return bodyOwner(
					ROOT_PATH.bind(location, p.getScope()).target(p));
		}

		p.getContext().getLogger().unresolvedScope(ref, type.getSign());

		return null;
	}

	@Override
	public final Owner visitParentRef(ParentRefNode ref, Distributor p) {

		final Location location = location(p, ref);
		final Path parentPath = parentPath(
				ip(),
				location,
				ref.getName().getName(),
				p.getContainer());

		return owner(parentPath.bind(location, p.getScope()).target(p));
	}

	@Override
	public final Owner visitIntrinsicRef(IntrinsicRefNode ref, Distributor p) {
		if ("object".equals(ref.getName().getName())) {
			return owner(this.refVisitor.objectIntrinsic(ref, p));
		}
		return super.visitIntrinsicRef(ref, p);
	}

	@Override
	public Owner visitMemberRef(MemberRefNode ref, Distributor p) {

		final RefNode declaredInNode = ref.getDeclaredIn();
		final Owner owner;
		final ExpressionNode ownerNode = ref.getOwner();

		if (ownerNode != null) {
			if (declaredInNode == null) {

				final NameNode nameNode = ref.getName();

				if (nameNode != null && isRootRef(ownerNode)) {

					final String name = nameNode.getName();

					if ("void".equals(name)) {
						return bodyOwner(voidRef(location(p, ref), p));
					}
					if ("false".equals(name)) {
						return bodyOwner(falseRef(location(p, ref), p));
					}
				}
			}

			owner = ownerNode.accept(this, p);
			if (owner == null) {
				return null;
			}
		} else {
			owner = null;
		}

		final StaticTypeRef declaredIn =
				this.refVisitor.declaredIn(declaredInNode, p);

		if (owner != null) {
			return owner.member(
					location(p, ref),
					this.refVisitor.ip().memberName(ref.getName().getName()),
					declaredIn);
		}

		return owner(new MemberById(
				this.refVisitor.ip(),
				location(p, ref.getName()),
				p,
				this.refVisitor.ip().memberName(ref.getName().getName()),
				declaredIn).toRef());
	}

	@Override
	public Owner visitAdapterRef(AdapterRefNode ref, Distributor p) {

		final Owner owner = ref.getOwner().accept(this, p);

		if (owner == null) {
			return null;
		}

		final Ref type =
				ref.getType().accept(this.refVisitor.adapterTypeVisitor(), p);

		if (type == null) {
			return null;
		}

		return owner.member(
				location(p, ref),
				adapterId(type.toStaticTypeRef()),
				this.refVisitor.declaredIn(ref.getDeclaredIn(), p));
	}

	@Override
	public Owner visitBodyRef(BodyRefNode ref, Distributor p) {

		final Owner result = ref.getOwner().accept(this, p);

		if (result == null) {
			return null;
		}

		return result.body(location(p, ref.getSuffix()));
	}

	@Override
	protected Owner visitRef(RefNode ref, Distributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return bodyOwner(errorRef(location(p, ref), p));
	}

	@Override
	protected Owner visitExpression(
			ExpressionNode expression,
			Distributor p) {
		return owner(
				expression.accept(this.refVisitor.ip().expressionVisitor(), p));
	}

	private final Owner owner(Ref ref) {
		return this.refVisitor.owner(ref);
	}

	private final Owner bodyOwner(Ref ref) {
		return this.refVisitor.bodyOwner(ref);
	}

}
