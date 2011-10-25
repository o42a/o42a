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
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;


public class OutcomeBuilder implements PathWalker {

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
	public boolean root(BoundPath path, Scope root) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean skip(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
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
	public boolean member(Container container, Step step, Member member) {

		final Clause containerClause = this.container.toClause();

		if (containerClause != null) {

			final MemberKey containerKey = containerClause.toMember().getKey();
			final MemberKey key = member.getKey();

			if (containerKey.startsWith(key)) {
				return up(container, step, member.substance(dummyUser()));
			}

			final Scope memberScope = member.getScope();

			if (this.container.getScope() == memberScope) {
				// Clause in the same scope.
				// Enclosing scope as a topmost container.

				final MemberContainer memberContainer =
						memberScope.getContainer();

				if (!up(this.container, step, memberContainer)) {
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
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return invalidOutcome();
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return invalidOutcome();
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		if (!initOutcome()) {
			return false;
		}

		this.outcome = this.outcome.materialize();

		return true;
	}

	@Override
	public boolean object(Step step, Obj object) {
		return invalidOutcome();
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
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
