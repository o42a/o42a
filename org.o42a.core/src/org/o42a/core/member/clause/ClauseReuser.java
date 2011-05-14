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
package org.o42a.core.member.clause;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;


final class ClauseReuser implements PathWalker {

	private final LocationInfo location;
	private Container container;
	private ReusedClause reused;

	ClauseReuser(Ref location) {
		this.location = location;
		this.container = location.getContainer();
	}

	public ReusedClause getReused() {
		return this.reused;
	}

	@Override
	public boolean root(Path path, Scope root) {
		getLogger().unexpectedAbsolutePath(this.location);
		return false;
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		getLogger().unexpectedAbsolutePath(this.location);
		return false;
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		if (this.reused != null) {
			// wrong path
			getLogger().invalidClauseReused(this.location);
			return false;
		}
		if (this.container.toClause() == null) {
			// can only reuse clauses within the same object
			getLogger().invalidClauseReused(this.location);
			return false;
		}
		if (enclosing.toClause() == null) {
			if (this.container.toClause().getKind() == ClauseKind.EXPRESSION) {
				// Topmost clause already reached.
				getLogger().invalidClauseReused(this.location);
				return false;
			}
			this.container = enclosing;
			return true;
		}

		this.container = enclosing;

		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {

		final Clause containerClause = this.container.toClause();

		if (containerClause != null) {

			final MemberKey containerKey = containerClause.toMember().getKey();
			final MemberKey key = member.getKey();

			if (containerKey.startsWith(key)) {
				return up(container, fragment, member.substance(dummyUser()));
			}
		}

		if (this.reused != null) {
			if (!this.reused.isObject()
					&& this.reused.getClause().getKind()
					== ClauseKind.EXPRESSION) {
				// Can only reuse (sub-) aliases or groups declared in
				// enclosing object or clause.
				getLogger().invalidClauseReused(this.location);
				return false;
			}
		}

		final Clause clause = member.toClause();

		if (clause == null) {
			getLogger().notClause(this.location);
			return false;
		}

		this.reused = new ReusedClause(this.container.toClause(), clause);

		return true;
	}

	@Override
	public boolean dep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		getLogger().invalidClauseReused(this.location);
		return false;
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		getLogger().invalidClauseReused(this.location);
		return false;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		if (this.reused != null) {
			assert this.reused.getClause() == result.toClause() :
				"Wrong path resolution: " + result + ", but "
				+ this.reused + " expected";
			return true;
		}

		this.container.assertSameScope(result);

		final Clause clause = result.toClause();

		if (clause != null) {
			this.reused = new ReusedClause(clause, clause);
		} else {
			assert result.toObject() != null :
				"Object expected: " + result;
			this.reused = new ReusedClause();
		}

		return true;
	}

	private CompilerLogger getLogger() {
		return this.location.getContext().getLogger();
	}

}
