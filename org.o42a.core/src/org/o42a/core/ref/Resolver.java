/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.*;


public final class Resolver implements LocationInfo {

	public static ResolverFactory resolverFactory(Scope scope) {
		return new DefaultResolverFactory(scope);
	}

	private final ResolverFactory factory;
	private final Scope scope;
	private final PathWalker walker;

	Resolver(
			ResolverFactory factory,
			Scope scope,
			PathWalker walker) {
		this.factory = factory;
		this.scope = scope;
		this.walker = walker;
	}

	@Override
	public final Location getLocation() {
		return this.scope.getLocation();
	}

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

	public final PathResolver toPathResolver() {
		return pathResolver(getScope(), dummyRefUser());
	}

	public final FullResolver fullResolver(RefUser user, RefUsage usage) {

		final Resolver resolver =
				this.factory.walkingResolver(
						new RoleResolver(this, usage.getRole()));

		return new FullResolver(resolver, user, usage);
	}

	@Override
	public String toString() {
		if (this.scope == null) {
			return super.toString();
		}
		return "Resolver[" + this.scope + ']';
	}

	final ResolverFactory factory() {
		return this.factory;
	}

	private static final class DefaultResolverFactory
			extends ResolverFactory {

		DefaultResolverFactory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver(PathWalker walker) {
			return new Resolver(this, getScope(), walker);
		}

	}

}
