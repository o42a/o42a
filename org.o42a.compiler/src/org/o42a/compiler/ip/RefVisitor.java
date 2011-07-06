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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.ValuePartRef.valuePartRef;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.ref.*;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.LocationInfo;
import org.o42a.core.member.MemberId;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public class RefVisitor extends AbstractRefVisitor<Ref, Distributor> {

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
			return SELF_PATH.target(location, p);
		case PARENT:
			return new ParentRef(p.getContext(), ref, null, p);
		case MODULE:
			return new ModuleRef(location(p, ref), p);
		case ROOT:
			return ROOT_PATH.target(location(p, ref), p);
		}

		p.getContext().getLogger().unresolvedScope(ref, type.getSign());

		return null;
	}

	@Override
	public Ref visitParentRef(ParentRefNode ref, Distributor p) {
		return new ParentRef(p.getContext(), ref, ref.getName().getName(), p);
	}

	@Override
	public Ref visitIntrinsicRef(IntrinsicRefNode ref, Distributor p) {
		if ("object".equals(ref.getName().getName())) {
			return objectIntrinsic(ref, p);
		}
		return valuePartRef(ref, p);
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

	final void init(Interpreter ip) {
		this.ip = ip;
	}

	private final class OwnerVisitor
			extends AbstractExpressionVisitor<Owner, Distributor> {

		@Override
		public Owner visitMemberRef(MemberRefNode ref, Distributor p) {

			final Owner owner;
			final ExpressionNode ownerNode = ref.getOwner();

			if (ownerNode != null) {
				owner = ownerNode.accept(this, p);
				if (owner == null) {
					return null;
				}
			} else {
				owner = null;
			}

			final StaticTypeRef declaredIn = declaredIn(ref.getDeclaredIn(), p);

			if (owner != null) {
				return owner.memberRefOwner(
						location(p, ref),
						memberName(ref.getName().getName()),
						declaredIn);
			}

			return new Owner(new MemberById(
					location(p, ref.getName()),
					p,
					memberName(ref.getName().getName()),
					declaredIn));
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

			final Ref parentRef =
				new ParentRef(p.getContext(), ref, ref.getName().getName(), p);

			return new Owner(parentRef, false);
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
			return new OverriddenEx(
					this.owner,
					this.owner.distribute(),
					this.owner);
		}

		public final Owner memberRefOwner(
				LocationInfo location,
				MemberId memberId,
				StaticTypeRef declaredIn) {
			return wrap(new MemberRef(
					location,
					this.owner,
					memberId,
					declaredIn));
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

}
