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

import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.value.ValueKnowledge.*;

import org.o42a.core.Container;
import org.o42a.core.ContainerInfo;
import org.o42a.core.Scope;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.Location;
import org.o42a.core.value.ValueKnowledge;
import org.o42a.core.value.link.impl.LinkTarget;
import org.o42a.core.value.link.impl.RtLinkTarget;


public abstract class LinkData<L extends Link> implements ContainerInfo {

	private final L link;
	private TargetRef targetRef;

	public LinkData(L link) {
		this.link = link;
	}

	public LinkData(L link, TargetRef targetRef) {
		this.link = link;
		this.targetRef = targetRef;
		if (targetRef != null) {
			targetRef.assertSameScope(link);
		}
	}

	@Override
	public final Location getLocation() {
		return getLink().getLocation();
	}

	@Override
	public final Container getContainer() {
		return getLink().getContainer();
	}

	@Override
	public final Scope getScope() {
		return getLink().getScope();
	}

	public final L getLink() {
		return this.link;
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

	public final ValueKnowledge getKnowledge() {

		final TargetRef targetRef = getTargetRef();
		final Resolution resolution =
				targetRef.resolve(targetRef.getScope().resolver());
		final Obj target = resolution.toObject();

		if (target == null || target.isNone()) {
			return FALSE_VALUE;
		}
		if (target.getConstructionMode().isRuntime()) {
			if (!getLink().getValueType().isVariable()) {
				return RUNTIME_CONSTRUCTED_VALUE;
			}
			return VARIABLE_VALUE;
		}
		if (!getLink().getValueType().isVariable()) {
			return KNOWN_VALUE;
		}

		return INITIALLY_KNOWN_VALUE;
	}

	public final Obj createTarget() {
		if (getLink().isRuntime()) {
			return new RtLinkTarget(getLink());
		}

		final Obj target = getTargetRef().getTarget();

		if (target == null) {
			return getContext().getNone();
		}
		if (target.getConstructionMode().isRuntime()) {
			return new RtLinkTarget(getLink());
		}

		return new LinkTarget(this);
	}

	public void resolveAll(FullResolver resolver) {
		getTargetRef().resolveAll(resolver.setRefUsage(TARGET_REF_USAGE));
	}

	@Override
	public String toString() {
		if (this.link == null) {
			return super.toString();
		}
		return this.link.toString();
	}

	protected abstract TargetRef buildTargetRef();

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

		final Field field = getScope().toField();

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
				typeRef.getScope());

		final TypeRelation relation =
				typeRef.relationTo(this.targetRef.toTypeRef())
				.check(this.link.getLogger());

		if (!relation.isAscendant()) {
			if (!relation.isError()) {
				getLogger().notDerivedFrom(
						this.targetRef.getLocation(),
						typeRef);
			}
		}
	}

}
