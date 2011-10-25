/*
    Compiler Core
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
package org.o42a.core.ref.common;

import static org.o42a.core.Rescoper.upgradeRescoper;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.util.use.UserInfo;


public abstract class RescopableRef<R extends RescopableRef<R>>
		implements ScopeInfo, Rescopable<R> {

	private final Rescoper rescoper;
	private boolean allResolved;
	private Ref rescopedRef;

	public RescopableRef(Rescoper rescoper) {
		this.rescoper = rescoper;
	}

	@Override
	public final Scope getScope() {
		return this.rescoper.getFinalScope();
	}

	public final Rescoper getRescoper() {
		return this.rescoper;
	}

	public abstract Ref getRef();

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = getRef().rescope(getRescoper());
	}

	public final Artifact<?> artifact(UserInfo user) {

		final Resolution resolution = resolve(getScope().newResolver(user));

		return resolution.isError() ? null : resolution.toArtifact();
	}

	public final Resolution resolve(Resolver resolver) {
		return getRef().resolve(getRescoper().rescope(resolver));
	}

	public final Value<?> value(Resolver resolver) {
		return getRef().value(getRescoper().rescope(resolver));
	}

	@Override
	public R rescope(Rescoper rescoper) {

		final Rescoper oldRescoper = getRescoper();
		final Rescoper newRescoper = oldRescoper.and(rescoper);

		if (newRescoper.equals(oldRescoper)) {
			return self();
		}

		return create(newRescoper, rescoper);
	}

	@Override
	public R prefixWith(PrefixPath prefix) {
		return rescope(prefix.toRescoper());
	}

	@Override
	public R upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return self();
		}
		return rescope(upgradeRescoper(getScope(), scope));
	}

	public R rescope(Scope scope) {
		if (getScope() == scope) {
			return self();
		}
		return rescope(getScope().rescoperTo(scope));
	}

	public void resolveAll(Resolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getRescoper().resolveAll(resolver);
			fullyResolve(getRescoper().rescope(resolver));
		} finally {
			getContext().fullResolution().end();
		}
	}

	public R reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Scope rescoped =
				getRescoper().rescope(reproducer.getReproducingScope());
		final Reproducer rescopedReproducer = reproducer.reproducerOf(rescoped);

		if (rescopedReproducer == null) {
			reproducer.getLogger().notReproducible(this);
			return null;
		}

		final Rescoper rescoper = getRescoper().reproduce(reproducer);

		if (rescoper == null) {
			return null;
		}

		final Ref ref = getRef().reproduce(rescopedReproducer);

		if (ref == null) {
			return null;
		}

		return createReproduction(
				reproducer,
				rescopedReproducer,
				ref,
				rescoper);
	}

	public RefOp op(CodeDirs dirs, HostOp host) {

		final HostOp rescoped = getRescoper().write(dirs, host);

		return getRef().op(rescoped);
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

	@SuppressWarnings("unchecked")
	protected final R self() {
		return (R) this;
	}

	protected abstract R create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

	protected abstract void fullyResolve(Resolver resolver);

	protected abstract R createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Rescoper rescoper);

}
