/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public abstract class ObjectLink
		extends AbstractContainer
		implements PlaceInfo {

	private final Container enclosing;
	private final ScopePlace place;
	private LinkValueStruct valueStruct;
	private Obj target;

	public ObjectLink(LocationInfo location, Distributor distributor) {
		super(location);
		this.enclosing = distributor.getContainer();
		this.place = distributor.getPlace();
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
	public final ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	public abstract LinkValueType getValueType();

	public final LinkValueStruct getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}
		return this.valueStruct = getValueType().linkStruct(getTypeRef());
	}

	public boolean isRuntime() {
		return (getValueType().isVariable()
				|| getScope().getConstructionMode().isRuntime());
	}

	public abstract TypeRef getTypeRef();

	public final Obj getTarget() {
		if (this.target != null) {
			return this.target;
		}
		return this.target = createTarget();
	}

	@Override
	public final Member toMember() {
		return null;
	}

	@Override
	public final Artifact<?> toArtifact() {
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
	public final LocalScope toLocal() {
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
	public final Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer().member(
				user,
				accessor,
				memberId,
				declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return getEnclosingContainer().findMember(
				user,
				accessor,
				memberId,
				declaredIn);
	}

	public final ObjectLink findIn(Scope scope) {

		final Scope linkScope = getScope();

		if (linkScope == scope) {
			return this;
		}

		assertCompatible(scope);

		return findLinkIn(scope);
	}

	public abstract void resolveAll(Resolver resolver);

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	protected abstract Obj createTarget();

	protected abstract ObjectLink findLinkIn(Scope enclosing);

}
