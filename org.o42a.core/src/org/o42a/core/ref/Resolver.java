/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.common.RoleResolver;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public final class Resolver implements LocationInfo {

	private final Scope scope;
	private final PathWalker walker;

	public Resolver(Scope scope) {
		assert scope != null :
			"Resolution scope not specified";
		this.scope = scope;
		this.walker = DUMMY_PATH_WALKER;
	}

	public Resolver(Scope scope, PathWalker walker) {
		assert scope != null :
			"Resolution scope not specified";
		this.scope = scope;
		this.walker = walker != null ? walker : DUMMY_PATH_WALKER;
	}

	@Override
	public final Location getLocation() {
		return this.scope.getLocation();
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

	public final PathResolver toPathResolver() {
		return pathResolver(getScope(), dummyUser());
	}

	public final FullResolver fullResolver(UserInfo user, RefUsage usage) {

		final Resolver resolver = new Resolver(
				getScope(),
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

}
