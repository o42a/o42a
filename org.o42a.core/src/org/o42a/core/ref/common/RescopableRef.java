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
package org.o42a.core.ref.common;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
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
		implements ScopeInfo {

	private final PrefixPath prefix;
	private boolean allResolved;
	private Ref rescopedRef;

	public RescopableRef(PrefixPath prefix) {
		this.prefix = prefix;
	}

	@Override
	public final Scope getScope() {
		return this.prefix.getStart();
	}

	public final PrefixPath getPrefix() {
		return this.prefix;
	}

	public abstract Ref getRef();

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = getRef().prefixWith(getPrefix());
	}

	public final Artifact<?> artifact(UserInfo user) {

		final Resolution resolution = resolve(getScope().newResolver(user));

		return resolution.isError() ? null : resolution.toArtifact();
	}

	public final Resolution resolve(Resolver resolver) {
		return getRef().resolve(getPrefix().rescope(resolver));
	}

	public final Value<?> value(Resolver resolver) {
		return getRef().value(getPrefix().rescope(resolver));
	}

	public R prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return self();
		}

		final PrefixPath oldPrefix = getPrefix();
		final PrefixPath newPrefix = oldPrefix.and(prefix);

		if (newPrefix == oldPrefix) {
			return self();
		}

		return create(newPrefix, prefix);
	}

	public R upgradeScope(Scope toScope) {
		if (toScope == getScope()) {
			return self();
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public R rescope(Scope scope) {
		if (getScope() == scope) {
			return self();
		}
		return prefixWith(scope.pathTo(getScope()));
	}

	public void resolveAll(Resolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			getPrefix().resolveAll(resolver);
			fullyResolve(getPrefix().rescope(resolver));
		} finally {
			getContext().fullResolution().end();
		}
	}

	public R reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Scope rescoped =
				getPrefix().rescope(reproducer.getReproducingScope());
		final Reproducer rescopedReproducer = reproducer.reproducerOf(rescoped);

		if (rescopedReproducer == null) {
			reproducer.getLogger().notReproducible(this);
			return null;
		}

		final PrefixPath prefix = getPrefix().reproduce(reproducer);

		if (prefix == null) {
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
				prefix);
	}

	public RefOp op(CodeDirs dirs, HostOp host) {

		final HostOp rescoped = getPrefix().write(dirs, host);

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

	protected abstract R create(PrefixPath prefix, PrefixPath additionalPrefix);

	protected abstract void fullyResolve(Resolver resolver);

	protected abstract R createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			PrefixPath prefix);

}
