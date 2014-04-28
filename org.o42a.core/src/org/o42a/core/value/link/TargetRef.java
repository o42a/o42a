/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.value.Value;


public final class TargetRef implements ScopeInfo {

	public static TargetRef targetRef(Ref ref, TypeRef typeRef) {
		if (typeRef != null) {
			return new TargetRef(ref, typeRef);
		}
		return new TargetRef(ref, ref.getInterface());
	}

	private final Ref ref;
	private final TypeRef typeRef;
	private boolean allResolved;

	private TargetRef(Ref ref, TypeRef typeRef) {
		this.ref = ref;
		this.typeRef = typeRef;
		typeRef.assertSameScope(this);
	}

	@Override
	public final Location getLocation() {
		return getRef().getLocation();
	}

	@Override
	public final Scope getScope() {
		return getRef().getScope();
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final boolean isStatic() {
		return getRef().isStatic();
	}

	public final Obj getTarget() {

		final Resolution resolution = resolve(getScope().resolver());

		return resolution.isError() ? null : resolution.toObject();
	}

	public final TypeRef toTypeRef() {
		return getRef().toTypeRef();
	}

	public final TargetRef toStatic() {
		return new TargetRef(
				getRef().toStatic(),
				this.typeRef.toStatic());
	}

	public final Resolution resolve(Resolver resolver) {
		return getRef().resolve(resolver);
	}

	public final Value<?> value(Resolver resolver) {
		return getRef().value(resolver);
	}

	public final TargetRef prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new TargetRef(
				getRef().prefixWith(prefix),
				getTypeRef().prefixWith(prefix));
	}

	public final TargetRef upgradeScope(Scope toScope) {
		if (toScope.is(getScope())) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public final void resolveAll(FullResolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getTypeRef().resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
			getRef().resolveAll(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final boolean assertFullyResolved() {
		assert this.allResolved :
			this + " is not fully resolved";
		return true;
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return "(" + this.typeRef + ") " + this.ref;
	}

}
