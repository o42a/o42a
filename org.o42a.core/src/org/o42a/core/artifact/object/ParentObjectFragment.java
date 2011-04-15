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
package org.o42a.core.artifact.object;

import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.Reproducer;


final class ParentObjectFragment extends MemberFragment {

	ParentObjectFragment(MemberKey memberKey) {
		super(memberKey);
	}

	@Override
	public Container resolve(
			LocationInfo location,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = start.getContainer().toObject();

		if (!object.membersResolved()) {

			final Scope self = getMemberKey().getOrigin();

			if (start == self) {

				final Container result = self.getEnclosingContainer();

				walker.up(object, this, result);

				return result;
			}
		}

		final Member member = resolveMember(location, path, index, start);

		if (member == null) {
			return null;
		}

		final Container result = member.getSubstance();

		walker.up(object, this, result);

		return result;
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope origin,
			Scope scope) {

		final Clause fromClause = origin.getContainer().toClause();

		if (fromClause == null) {
			// Walked out of object, containing clauses.
			if (!reproducer.phraseCreatesObject()) {
				return outOfClausePath(SELF_PATH, toPath());
			}
			return outOfClausePath(
					scope.getEnclosingScopePath(),
					toPath());

		}

		final Clause enclosingClause = fromClause.getEnclosingClause();

		if (enclosingClause == null && !fromClause.requiresInstance()) {
			// Left stand-alone clause without enclosing object.
			return outOfClausePath(
					scope.getEnclosingScopePath(),
					SELF_PATH);
		}

		// Update to actual enclosing scope path.
		return reproducedPath(scope.getEnclosingScopePath());
	}

}
