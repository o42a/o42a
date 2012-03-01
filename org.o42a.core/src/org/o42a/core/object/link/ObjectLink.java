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
import static org.o42a.core.source.CompilerLogger.logDeclaration;
import static org.o42a.core.value.ValueKnowledge.*;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.object.link.impl.LinkTarget;
import org.o42a.core.object.link.impl.RuntimeLinkTarget;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public abstract class ObjectLink
		extends AbstractContainer
		implements PlaceInfo {

	private final Container enclosing;
	private final ScopePlace place;
	private TargetRef targetRef;
	private Obj target;
	private LinkValueStruct valueStruct;

	public ObjectLink(LocationInfo location, Distributor distributor) {
		this(location, distributor, null);
	}

	public ObjectLink(
			LocationInfo location,
			Distributor distributor,
			TargetRef targetRef) {
		super(location);
		this.enclosing = distributor.getContainer();
		this.place = distributor.getPlace();
		this.targetRef = targetRef;
		if (targetRef != null) {
			targetRef.assertSameScope(this);
		}
	}

	protected ObjectLink(ObjectLink prototype, TargetRef targetRef) {
		this(
				new Location(
						targetRef.getContext(),
						targetRef.getScope().getLoggable().setReason(
								logDeclaration(prototype))),
				prototype.distributeIn(targetRef.getScope().getContainer()),
				targetRef);
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

	public final TypeRef getTypeRef() {
		return getTargetRef().getTypeRef();
	}

	public final TargetRef getTargetRef() {
		if (this.targetRef == null) {
			define();
		}
		return this.targetRef;
	}

	public final Obj getTarget() {
		if (this.target != null) {
			return this.target;
		}
		return this.target = createTarget();
	}

	public ValueKnowledge getKnowledge() {

		final TargetRef targetRef = getTargetRef();
		final Resolution resolution =
				targetRef.resolve(targetRef.getScope().dummyResolver());

		if (resolution.isError() || !resolution.isResolved()) {
			return UNKNOWN_VALUE;
		}

		final Obj target = resolution.materialize();

		if (target == null) {
			return UNKNOWN_VALUE;
		}
		if (target.getConstructionMode().isRuntime()) {
			if (!getValueType().isVariable()) {
				return RUNTIME_CONSTRUCTED_VALUE;
			}
			return VARIABLE_VALUE;
		}
		if (!getValueType().isVariable()) {
			return KNOWN_VALUE;
		}

		return INITIALLY_KNOWN_VALUE;
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

	public final Value<ObjectLink> toValue() {
		return getValueStruct().compilerValue(this);
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

	public void resolveAll(Resolver resolver) {
		getTypeRef().resolveAll(resolver);
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

	protected abstract ObjectLink prefixWith(PrefixPath prefix);

	private void define() {
		this.targetRef = buildTargetRef();
		if (this.targetRef == null) {
			getLogger().error(
					"missing_link_target",
					this,
					"Link target is missing");
			this.targetRef = falseRef(
					this,
					distribute())
					.toTargetRef(null);
			return;
		}
		this.targetRef.assertScopeIs(getScope());

		final Field<?> field = getScope().toField();

		if (field == null || !(field.isAbstract() || field.isPrototype())) {
			Role.INSTANCE.checkUseBy(
					this,
					this.targetRef.getRef(),
					this.targetRef.getPrefix().rescope(
							this.targetRef.getScope()));
		}

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

	private Obj createTarget() {
		if (isRuntime()) {
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
