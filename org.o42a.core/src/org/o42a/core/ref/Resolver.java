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

import org.o42a.core.*;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.User;
import org.o42a.util.use.UserInfo;


public class Resolver implements UserInfo, LocationInfo {

	public static ResolverFactory<Resolver> resolverFactory(Scope scope) {
		return new Factory(scope);
	}

	private final Scope scope;
	private final User user;

	protected Resolver(Scope scope, UserInfo user) {
		this.scope = scope;
		this.user = user.toUser();
	}

	@Override
	public final Loggable getLoggable() {
		return this.scope.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.scope.getContext();
	}

	public final boolean isDummy() {
		return this.user.isDummy();
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

	public final boolean assertNotDummy() {
		assert !isDummy() :
			this + " is dummy";
		return true;
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}
		if (isDummy()) {
			return "DummyResolver[" + this.scope + ']';
		}
		return "Resolver[" + this.scope + " by " + this.user + ']';
	}

	private static final class Factory extends ResolverFactory<Resolver> {

		Factory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver(UserInfo user) {
			return new Resolver(getScope(), user);
		}

	}

}
