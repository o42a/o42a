/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ref.path.PathResolver.fullPathResolver;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public final class FullResolver implements LocationInfo {

	private final Resolver resolver;
	private final RefUser user;
	private final RefUsage usage;

	FullResolver(Resolver resolver, RefUser user, RefUsage usage) {
		this.resolver = resolver;
		this.user = user;
		this.usage = usage;
	}

	@Override
	public final Location getLocation() {
		return getResolver().getLocation();
	}

	public final Container getContainer() {
		return getResolver().getContainer();
	}

	public final Scope getScope() {
		return getResolver().getScope();
	}

	public final Resolver getResolver() {
		return this.resolver;
	}

	public RefUser refUser() {
		return this.user;
	}

	public final RefUsage refUsage() {
		return this.usage;
	}

	public FullResolver setRefUsage(RefUsage refUsage) {
		if (refUsage() == refUsage) {
			return this;
		}
		return getResolver().fullResolver(this.user, refUsage);
	}

	public final PathResolver toPathResolver() {
		return fullPathResolver(getScope(), refUser(), refUsage());
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}
		return "FullResolver[" + getScope()
				+ " by " + refUser()
				+ " for " + refUsage()  + ']';
	}

}
