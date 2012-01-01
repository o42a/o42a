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
import org.o42a.compiler.ip.ref.MemberOf;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public class RefVisitor extends AbstractRefVisitor<Ref, Distributor> {

	private static final RootVisitor ROOT_VISITOR = new RootVisitor();

	private final OwnerVisitor ownerVisitor = new OwnerVisitor();
	private Interpreter ip;

	protected RefVisitor() {
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public Ref visitScopeRef(ScopeRefNode ref, Distributor p) {

		final ScopeType type = ref.getType();
		final Location location = location(p, ref);

		switch (type) {
		case IMPLIED:
			break;
		case SELF:
			return SELF_PATH.bind(location, p.getScope()).target(p);
		case PARENT:
			return parentPath(ip(), location, null, p.getContainer())
					.bind(location, p.getScope())
					.target(p);
		case MODULE:
			return enclosingModulePath(p.getContainer())
					.bind(location, p.getScope())
					.target(p);
		case ROOT:
			return ROOT_PATH.bind(location, p.getScope()).target(p);
		}

		p.getContext().getLogger().unresolvedScope(ref, type.getSign());

		return null;
	}

	@Override
	public Ref visitParentRef(ParentRefNode ref, Distributor p) {

		final Location location = location(p, ref);
		final Path parentPath = parentPath(
				ip(),
				location,
				ref.getName().getName(),
				p.getContainer());

		return parentPath.bind(location, p.getScope()).target(p);
	}

	@Override
	public Ref visitIntrinsicRef(IntrinsicRefNode ref, Distributor p) {
		if ("object".equals(ref.getName().getName())) {
			return objectIntrinsic(ref, p);
		}
		return super.visitIntrinsicRef(ref, p);
	}

	@Override
	public Ref visitMemberRef(MemberRefNode ref, Distributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.ref() : null;
	}

	@Override
	public Ref visitAdapterRef(AdapterRefNode ref, Distributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.ref() : null;
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return errorRef(location(p, ref), p);
	}

	protected Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {
		p.getLogger().error(
				"prohibited_object_intrinsic",
				ref,
				"$object$ intrinsic is allowed only within clauses");
		return errorRef(location(p, ref), p);
	}

	protected StaticTypeRef declaredIn(RefNode declaredInNode, Distributor p) {
		if (declaredInNode == null) {
			return null;
		}

		final Ref declaredIn = declaredInNode.accept(this, p);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	protected RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
		return this;
	}

	protected final void init(Interpreter ip) {
		this.ip = ip;
	}

	private final class OwnerVisitor
			extends AbstractExpressionVisitor<Owner, Distributor> {

		@Override
		public Owner visitMemberRef(MemberRefNode ref, Distributor p) {

			final RefNode declaredInNode = ref.getDeclaredIn();
			final Owner owner;
			final ExpressionNode ownerNode = ref.getOwner();

			if (ownerNode != null) {
				if (declaredInNode == null) {

					final NameNode nameNode = ref.getName();

					if (nameNode != null
							&& ownerNode.accept(ROOT_VISITOR, null) != null) {

						final String name = nameNode.getName();

						if ("void".equals(name)) {
							return new Owner(voidRef(location(p, ref), p));
						}
						if ("false".equals(name)) {
							return new Owner(falseRef(location(p, ref), p));
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

			final StaticTypeRef declaredIn = declaredIn(declaredInNode, p);

			if (owner != null) {
				return owner.memberRefOwner(
						location(p, ref),
						ip().memberName(ref.getName().getName()),
						declaredIn);
			}

			return new Owner(new MemberById(
					ip(),
					location(p, ref.getName()),
					p,
					ip().memberName(ref.getName().getName()),
					declaredIn).toRef());
		}

		@Override
		public Owner visitAdapterRef(AdapterRefNode ref, Distributor p) {

			final Owner owner = ref.getOwner().accept(this, p);

			if (owner == null) {
				return null;
			}

			final Ref type = ref.getType().accept(adapterTypeVisitor(), p);

			if (type == null) {
				return null;
			}

			return owner.memberRefOwner(
					location(p, ref),
					adapterId(type.toStaticTypeRef()),
					declaredIn(ref.getDeclaredIn(), p));
		}

		@Override
		public Owner visitScopeRef(ScopeRefNode ref, Distributor p) {

			final ScopeType type = ref.getType();

			switch (type) {
			case IMPLIED:
				break;
			case SELF:
			case PARENT:
			case MODULE:
			case ROOT:
				return super.visitScopeRef(ref, p);
			}

			p.getContext().getLogger().unresolvedScope(ref, type.getSign());

			return null;
		}

		@Override
		public Owner visitParentRef(ParentRefNode ref, Distributor p) {

			final Location location = location(p, ref);
			final Path parentPath = parentPath(
					ip(),
					location,
					ref.getName().getName(),
					p.getContainer());

			return new Owner(
					parentPath.bind(location, p.getScope()).target(p),
					false);
		}

		@Override
		protected Owner visitExpression(
				ExpressionNode expression,
				Distributor p) {
			return new Owner(
					expression.accept(ip().expressionVisitor(), p),
					false);
		}

	}

	static final class Owner {

		private final Ref owner;
		private final boolean overridden;

		Owner(Ref owner) {
			this.owner = owner;
			this.overridden = false;
		}

		Owner(Ref owner, boolean overridden) {
			this.owner = owner;
			this.overridden = overridden;
		}

		public Ref ref() {
			if (this.owner == null) {
				return null;
			}
			if (!this.overridden) {
				return this.owner;
			}
			throw new UnsupportedOperationException();
		}

		public final Owner memberRefOwner(
				LocationInfo location,
				MemberId memberId,
				StaticTypeRef declaredIn) {

			final MemberOf memberOf = new MemberOf(
					location,
					this.owner.distribute(),
					memberId,
					declaredIn);
			final BoundPath path =
					this.owner.getPath().append(memberOf);

			return wrap(path.setLocation(location)
					.target(this.owner.distribute()));
		}

		@Override
		public String toString() {
			if (!this.overridden) {
				return this.owner.toString();
			}
			return "(" + this.owner + ")^";
		}

		private Owner wrap(Ref owner) {
			return new Owner(owner, this.overridden);
		}

	}

	private static final class RootVisitor
			extends AbstractExpressionVisitor<Object, Void> {

		@Override
		public Object visitScopeRef(ScopeRefNode ref, Void p) {
			if (ref.getType() == ScopeType.ROOT) {
				return Boolean.TRUE;
			}
			return null;
		}

		@Override
		protected Object visitExpression(ExpressionNode expression, Void p) {
			return null;
		}

	}

}
