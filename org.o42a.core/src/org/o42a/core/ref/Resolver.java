/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref;

import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public class Resolver implements UserInfo, LocationInfo {

	public static ResolverFactory<Resolver, FullResolver> resolverFactory(
			Scope scope) {
		return new DefaultResolverFactory(scope);
	}

	private final ResolverFactory<?, ?> factory;
	private final Scope scope;
	private final User<?> user;
	private final PathWalker walker;

	protected Resolver(
			ResolverFactory<?, ?> factory,
			Scope scope,
			UserInfo user,
			PathWalker walker) {
		this.factory = factory;
		this.scope = scope;
		this.user = user.toUser();
		this.walker = walker;
	}

	@Override
	public final Loggable getLoggable() {
		return this.scope.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.scope.getContext();
	}

	public final Container getContainer() {
		return this.scope.getContainer();
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final PathWalker getWalker() {
		return this.walker;
	}

	public final CompilerLogger getLogger() {
		return this.scope.getLogger();
	}

	@Override
	public final User<?> toUser() {
		return this.user;
	}

	public final PathResolver toPathResolver() {
		return pathResolver(getScope(), this);
	}

	@SuppressWarnings("unchecked")
	public FullResolver fullResolver(RefUsage usage) {

		final Resolver resolver = this.factory.walkingResolver(
				this,
				new RoleResolver(usage.getRole()));

		return factory().createFullResolver(resolver, usage);
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}
		if (toUser().isDummy()) {
			return "DummyResolver[" + this.scope + ']';
		}
		return "Resolver[" + this.scope + " by " + this.user + ']';
	}

	@SuppressWarnings("rawtypes")
	final ResolverFactory factory() {
		return this.factory;
	}

	private static final class DefaultResolverFactory
			extends ResolverFactory<Resolver, FullResolver> {

		DefaultResolverFactory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver(
				UserInfo user,
				PathWalker walker) {
			return new Resolver(this, getScope(), user, walker);
		}

		@Override
		protected FullResolver createFullResolver(
				Resolver resolver,
				RefUsage refUsage) {
			return new FullResolver(resolver, refUsage);
		}

	}

}
