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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.Ref.falseRef;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.object.link.impl.LinkTarget;
import org.o42a.core.object.link.impl.RuntimeLinkTarget;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;


public abstract class ObjectLink
		extends AbstractContainer
		implements PlaceInfo {

	private final Container enclosing;
	private final ScopePlace place;
	private TargetRef targetRef;
	private Obj materialization;

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

	public abstract boolean isVariable();

	public final TypeRef getTypeRef() {
		return getTargetRef().getTypeRef();
	}

	public final TargetRef getTargetRef() {
		if (this.targetRef == null) {
			define();
		}
		return this.targetRef;
	}

	public final Obj materialize() {
		if (this.materialization != null) {
			return this.materialization;
		}
		return this.materialization = createMaterialization();
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

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	protected abstract TargetRef buildTargetRef();

	protected abstract ObjectLink findLinkIn(Scope enclosing);

	private void define() {
		this.targetRef = buildTargetRef();
		if (this.targetRef == null) {
			getLogger().error(
					"missing_link_target",
					this,
					"Link target is missing");
			this.targetRef = falseRef(
					this,
					getScope().getEnclosingScope().distribute())
					.toTargetRef(null);
			return;
		}
		this.targetRef.assertSameScope(getScope().getEnclosingScope());

		this.targetRef.assertScopeIs(getScope().getEnclosingScope());
		Role.INSTANCE.checkUseBy(
				this,
				this.targetRef.getRef(),
				this.targetRef.getPrefix().rescope(
						this.targetRef.getScope()));

		final TypeRef typeRef = this.targetRef.getTypeRef();

		Role.PROTOTYPE.checkUseBy(
				this,
				typeRef.getRef(),
				typeRef.getPrefix().rescope(typeRef.getScope()));

		final TypeRelation relation =
				typeRef.relationTo(this.targetRef.toTypeRef());

		if (!relation.isAscendant()) {
			if (!relation.isError()) {
				getLogger().notDerivedFrom(this.targetRef, typeRef);
			}
		}
	}

	private Obj createMaterialization() {
		if (isVariable() || getScope().getConstructionMode().isRuntime()) {
			return new RuntimeLinkTarget(this);
		}

		final Artifact<?> artifact = getTargetRef().artifact(dummyUser());

		if (artifact == null) {
			return getContext().getFalse();
		}

		final Obj target = artifact.materialize();

		if (target.getConstructionMode().isRuntime()) {
			return new RuntimeLinkTarget(this);
		}

		return new LinkTarget(this);
	}

}
