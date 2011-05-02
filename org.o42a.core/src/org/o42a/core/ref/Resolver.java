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

import org.o42a.core.CompilerLogger;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.util.use.User;
import org.o42a.util.use.UserInfo;


public class Resolver implements UserInfo {

	public static ResolverFactory<Resolver> resolverFactory(Scope scope) {
		return new Factory(scope);
	}

	private final Scope scope;
	private final User user;

	protected Resolver(Scope scope, UserInfo user) {
		this.scope = scope;
		this.user = user.toUser();
	}

	public final Container getContainer() {
		return this.scope.getContainer();
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final CompilerLogger getLogger() {
		return this.scope.getLogger();
	}

	@Override
	public final User toUser() {
		return this.user;
	}

	@Override
	public String toString() {
		return "Resolver[" + this.scope + ']';
	}

	private static final class Factory extends ResolverFactory<Resolver> {

		Factory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver() {
			return new Resolver(getScope(), this);
		}

	}

}
