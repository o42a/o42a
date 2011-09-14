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
package org.o42a.core.member.impl.clause;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ResolutionWalker;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;


public class OutcomeBuilder implements ResolutionWalker, PathWalker {

	private final LocationInfo location;
	private Container container;
	private Path outcome;

	public OutcomeBuilder(Ref location) {
		this.location = location;
		this.container = location.getContainer();
	}

	public final Path getOutcome() {
		return this.outcome;
	}

	@Override
	public PathWalker path(LocationInfo location, Path path) {
		return this;
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {
		return invalidOutcome();
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		return invalidOutcome();
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {
		return invalidOutcome();
	}

	@Override
	public boolean root(Path path, Scope root) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		if (this.outcome != null) {
			return invalidOutcome();
		}

		final Clause containerClause = this.container.toClause();

		if (containerClause == null) {
			return invalidOutcome();
		}
		if (enclosing.toClause() == null
				&& containerClause.getKind() == ClauseKind.EXPRESSION) {
			return invalidOutcome();
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

			final Scope memberScope = member.getScope();

			if (this.container.getScope() == memberScope) {
				// Clause in the same scope.
				// Enclosing scope as a topmost container.

				final MemberContainer memberContainer =
						memberScope.getContainer();

				if (!up(this.container, fragment, memberContainer)) {
					return false;
				}
			}
		}

		final Field<?> field = member.toField(dummyUser());

		if (field == null) {
			return invalidOutcome();
		}

		if (!initOutcome()) {
			return false;
		}

		this.outcome = this.outcome.append(field.getKey());

		return true;
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return invalidOutcome();
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		return invalidOutcome();
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		if (!initOutcome()) {
			return false;
		}

		this.outcome = this.outcome.materialize();

		return true;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return initOutcome();
	}

	private CompilerLogger getLogger() {
		return this.location.getContext().getLogger();
	}

	private final boolean initOutcome() {
		if (this.outcome != null) {
			return true;
		}

		this.outcome = pathInObject(this.container);

		return this.outcome != null;
	}

	private Path pathInObject(Container container) {

		final Clause clause = container.toClause();

		if (clause == null) {
			return Path.SELF_PATH;
		}

		return pathInObject(clause);
	}

	private Path pathInObject(Clause clause) {
		switch (clause.getKind()) {
		case EXPRESSION:
			if (clause.isTopLevel()) {
				return Path.SELF_PATH;
			}

			invalidOutcome();

			return null;
		case GROUP:
			return pathInObject(clause.getEnclosingClause());
		case OVERRIDER:

			final MemberKey overridden =
					clause.toPlainClause().getOverridden();
			final Path enclosingPathInObject =
					pathInObject(clause.getEnclosingClause());

			return enclosingPathInObject.materialize().append(overridden);
		}

		throw new IllegalStateException(
				"Clause of unsupported kind: " + clause);
	}

	private boolean invalidOutcome() {
		getLogger().error(
				"invalid_clause_outcome",
				this.location,
				"Clause outcome should be a constructed object's field");
		return false;
	}

	private boolean unexpectedAbsolutePath() {
		getLogger().error(
				"unexpected_clause_outcome_absolute_path",
				this.location,
				"Clause outcome should be a relative path");
		return false;
	}

}
