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

import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.util.use.UserInfo;


public abstract class ResolverFactory<R extends Resolver> {

	private final Scope scope;
	private R dummyResolver;

	public ResolverFactory(Scope scope) {
		this.scope = scope;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final R dummyResolver() {
		if (this.dummyResolver != null) {
			return this.dummyResolver;
		}
		return this.dummyResolver =
				createResolver(dummyUser(), DUMMY_PATH_WALKER);
	}

	public final R newResolver(UserInfo user) {
		if (user.toUser().isDummy()) {
			return dummyResolver();
		}
		return createResolver(user, DUMMY_PATH_WALKER);
	}

	public final R walkingResolver(UserInfo user, PathWalker walker) {
		if (walker == null || walker == DUMMY_PATH_WALKER) {
			return newResolver(user);
		}
		return createResolver(user, walker);
	}

	@Override
	public String toString() {
		if (this.scope == null) {
			return super.toString();
		}
		return "ResolverFactory[" + this.scope + ']';
	}

	protected abstract R createResolver(UserInfo user, PathWalker walker);

}
