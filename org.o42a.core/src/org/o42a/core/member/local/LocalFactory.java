/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.member.local;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.impl.ExplicitLocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.string.Name;


public final class LocalFactory {

	private final MemberRegistry memberRegistry;

	public LocalFactory(MemberRegistry memberRegistry) {
		this.memberRegistry = memberRegistry;
	}

	public LocalScope newLocalScope(
			LocationInfo location,
			Distributor distributor,
			Name name) {

		final Obj owner = owner(location);

		if (owner == null) {
			return null;
		}

		final ExplicitLocalScope explicitScope = new ExplicitLocalScope(
				location,
				distributor,
				owner,
				name != null ? name : this.memberRegistry.anonymousBlockName());

		this.memberRegistry.declareMember(explicitScope.toMember());

		return explicitScope;
	}

	public LocalScope reproduceLocalScope(
			Reproducer reproducer,
			LocalScope scope) {

		final Obj owner = owner(reproducer.getScope());

		if (owner == null) {
			return null;
		}

		final ExplicitLocalScope reproducedScope = new ExplicitLocalScope(
				scope,
				reproducer.distribute(),
				owner,
				scope.explicit());

		this.memberRegistry.declareMember(reproducedScope.toMember());

		return reproducedScope;
	}

	private Obj owner(LocationInfo location) {

		final Obj owner = this.memberRegistry.getOwner();

		if (owner == null) {
			location.getLocation()
			.getLogger()
			.prohibitedLocal(location.getLocation());
		}

		return owner;
	}

}
