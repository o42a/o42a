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

import org.o42a.core.Distributor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.object.link.impl.LinkTarget;
import org.o42a.core.object.link.impl.RuntimeLinkTarget;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public abstract class KnownLink extends ObjectLink {

	private TargetRef targetRef;

	public KnownLink(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public KnownLink(
			LocationInfo location,
			Distributor distributor,
			TargetRef targetRef) {
		super(location, distributor);
		this.targetRef = targetRef;
		if (targetRef != null) {
			targetRef.assertSameScope(this);
		}
	}

	protected KnownLink(ObjectLink prototype, TargetRef targetRef) {
		this(
				new Location(
						targetRef.getContext(),
						targetRef.getScope().getLoggable().setReason(
								logDeclaration(prototype))),
				prototype.distributeIn(targetRef.getScope().getContainer()),
				targetRef);
	}

	@Override
	public final TypeRef getTypeRef() {
		return getTargetRef().getTypeRef();
	}

	public final TargetRef getTargetRef() {
		if (this.targetRef == null) {
			define();
		}
		return this.targetRef;
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

	public final Value<KnownLink> toValue() {
		return getValueStruct().compilerValue(this);
	}

	@Override
	public void resolveAll(Resolver resolver) {
		getTargetRef().resolveAll(resolver);
	}

	protected abstract KnownLink prefixWith(PrefixPath prefix);

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
					this.targetRef.getScope());
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

	protected abstract TargetRef buildTargetRef();

	@Override
	protected final Obj createTarget() {
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
