/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.link;

import static org.o42a.util.fn.Init.init;

import org.o42a.core.*;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.fn.Init;


public abstract class Link extends AbstractContainer implements ContainerInfo {

	private final Container enclosing;
	private final LinkTargetNesting targetNesting = new LinkTargetNesting(this);
	private final Init<TypeParameters<KnownLink>> typeParameters =
			init(() -> getValueType().typeParameters(getInterfaceRef()));
	private final Init<Obj> target = init(this::createTarget);

	public Link(LocationInfo location, Distributor distributor) {
		super(location);
		this.enclosing = distributor.getContainer();
	}

	@Override
	public final Scope getScope() {
		return getEnclosingContainer().getScope();
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.enclosing;
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	public final Nesting getTargetNesting() {
		return this.targetNesting;
	}

	public abstract LinkValueType getValueType();

	public final TypeParameters<KnownLink> getLinkParameters() {
		return this.typeParameters.get();
	}

	public abstract boolean isSynthetic();

	public boolean isRuntime() {
		return (getValueType().isVariable()
				|| getScope().getConstructionMode().isRuntime());
	}

	public abstract TypeRef getInterfaceRef();

	public final Obj getTarget() {
		return this.target.get();
	}

	@Override
	public final Member toMember() {
		return null;
	}

	@Override
	public final Obj toObject() {
		return null;
	}

	@Override
	public final Clause toClause() {
		return null;
	}

	@Override
	public final Namespace toNamespace() {
		return null;
	}

	@Override
	public final Member member(MemberKey memberKey) {
		return null;
	}

	@Override
	public final MemberPath member(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer().member(
				access,
				memberId,
				declaredIn);
	}

	@Override
	public MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer().findMember(
				access,
				memberId,
				declaredIn);
	}

	public final Link findIn(Scope scope) {
		if (getScope().is(scope)) {
			return this;
		}

		assertCompatible(scope);

		return findLinkIn(scope);
	}

	public abstract void resolveAll(FullResolver resolver);

	protected abstract Obj createTarget();

	protected abstract Link findLinkIn(Scope enclosing);

	private static final class LinkTargetNesting implements Nesting {

		private final Link link;

		LinkTargetNesting(Link link) {
			this.link = link;
		}

		@Override
		public Obj findObjectIn(Scope enclosing) {
			return this.link.findIn(enclosing).getTarget();
		}

		@Override
		public String toString() {
			if (this.link == null) {
				return super.toString();
			}
			return this.link.toString();
		}

	}

}
