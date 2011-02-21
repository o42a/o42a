/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.member.local.LocalScope.explicitScope;
import static org.o42a.core.member.local.LocalScope.reproducedScope;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.clause.MemberRegistryClauseBase;
import org.o42a.core.st.Reproducer;


public abstract class MemberRegistryLocalBase
		extends MemberRegistryClauseBase {

	public abstract Obj getOwner();

	public LocalScope newLocalScope(
			LocationInfo location,
			Distributor distributor,
			String name) {

		final Obj owner = owner(location);

		if (owner == null) {
			return null;
		}

		final ExplicitLocalScope explicitScope = explicitScope(
				location,
				distributor,
				owner,
				name != null ? name : anonymousBlockName());

		declareMember(explicitScope.toMember());

		return explicitScope;
	}

	public LocalScope reproduceLocalScope(
			Reproducer reproducer,
			LocalScope scope) {

		final Obj owner = owner(reproducer.getScope());

		if (owner == null) {
			return null;
		}

		final ExplicitLocalScope reproducedScope =
			reproducedScope(
					scope,
					reproducer.distribute(),
					owner,
					scope.explicit());

		declareMember(reproducedScope.toMember());

		return reproducedScope;
	}

	public abstract void declareMember(Member member);

	public abstract boolean declareBlock(LocationInfo location, String name);

	public abstract String anonymousBlockName();

	private Obj owner(LocationInfo location) {

		final Obj owner = getOwner();

		if (owner == null) {
			location.getContext().getLogger().prohibitedLocal(location);
		}

		return owner;
	}

}
