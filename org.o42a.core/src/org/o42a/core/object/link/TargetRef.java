/*
    Compiler Core
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
package org.o42a.core.object.link;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.util.log.Loggable;


public final class TargetRef implements ScopeInfo {

	public static TargetRef targetRef(Ref ref, TypeRef typeRef) {
		if (typeRef != null) {
			return new TargetRef(ref, typeRef);
		}
		return new TargetRef(ref, ref.ancestor(ref));
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
	public final CompilerContext getContext() {
		return getRef().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getRef().getLoggable();
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

	public final TypeRef toTypeRef() {
		return getRef().toTypeRef();
	}

	public final TargetRef toStatic() {
		return new TargetRef(
				getRef().toStatic(),
				this.typeRef.toStatic());
	}

	public final Obj target(UserInfo user) {

		final Resolution resolution = resolve(getScope().newResolver(user));

		return resolution.isError() ? null : resolution.toObject();
	}

	public final Resolution resolve(Resolver resolver) {
		return getRef().resolve(resolver);
	}

	public final Value<?> value(Resolver resolver) {
		return getRef().value(resolver);
	}

	public TargetRef prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new TargetRef(
				getRef().prefixWith(prefix),
				getTypeRef().prefixWith(prefix));
	}

	public TargetRef upgradeScope(Scope toScope) {
		if (toScope == getScope()) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public TargetRef rescope(Scope scope) {
		if (getScope() == scope) {
			return this;
		}
		return prefixWith(scope.pathTo(getScope()));
	}

	public void resolveAll(Resolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getTypeRef().resolveAll(resolver);
			getRef().resolve(resolver).resolveTarget();
		} finally {
			getContext().fullResolution().end();
		}
	}

	public TargetRef reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = getRef().reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TargetRef(ref, typeRef);
	}

	public final RefOp ref(CodeDirs dirs, ObjOp host) {
		return getRef().op(host);
	}

	public final HostOp target(CodeDirs dirs, ObjOp host) {
		return ref(dirs, host).target(dirs);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
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
