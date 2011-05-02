/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.util.use.Usable;
import org.o42a.util.use.User;
import org.o42a.util.use.UserInfo;


public abstract class ResolverFactory<R extends Resolver> implements UserInfo {

	private final Scope scope;
	private final UsableResolver<R> usable;

	public ResolverFactory(Scope scope) {
		this.scope = scope;
		this.usable = new UsableResolver<R>(this);
	}

	public final Scope getScope() {
		return this.scope;
	}

	@Override
	public final User toUser() {
		return this.usable;
	}

	public final R newResolver(UserInfo user) {
		return usable().useBy(user);
	}

	@Override
	public String toString() {
		if (this.scope == null) {
			return super.toString();
		}
		return "ResolverFactory[" + this.scope + ']';
	}

	protected final Usable<R> usable() {
		return this.usable;
	}

	protected abstract R createResolver();

	private static final class UsableResolver<R extends Resolver>
			extends Usable<R> {

		private final ResolverFactory<R> factory;
		private R resolver;

		UsableResolver(ResolverFactory<R> factory) {
			this.factory = factory;
		}

		@Override
		public String toString() {
			if (this.factory == null) {
				return super.toString();
			}
			return "UsableResolver[" + this.factory.getScope() + ']';
		}

		@Override
		protected R createUsed(User user) {
			if (this.resolver != null) {
				return this.resolver;
			}
			return this.resolver = this.factory.createResolver();
		}

	}

}
