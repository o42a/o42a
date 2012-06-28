/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.analysis.use.User;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;


final class ClauseReuser implements PathWalker {

	private final LocationInfo location;
	private final boolean reuseContents;
	private Container container;
	private ReusedClause reused;

	ClauseReuser(Ref location, boolean reuseContents) {
		this.location = location;
		this.reuseContents = reuseContents;
		this.container = location.getContainer();
	}

	public ReusedClause getReused() {
		return this.reused;
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
	public boolean staticScope(Step step, Scope scope) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		if (this.reused != null) {
			return invalidClauseReused();
		}

		final Clause containerClause = this.container.toClause();

		if (containerClause == null) {
			return invalidClauseReused();
		}
		if (enclosing.toClause() == null
				&& containerClause.getKind() == ClauseKind.EXPRESSION) {
			return invalidClauseReused();
		}

		this.container = enclosing;

		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		final Clause containerClause = this.container.toClause();

		if (containerClause != null) {

			final MemberKey containerKey = containerClause.toMember().getMemberKey();
			final MemberKey key = member.getMemberKey();

			if (containerKey.startsWith(key)) {
				return up(container, step, member.substance(dummyUser()), null);
			}

			final Scope memberScope = member.getScope();

			if (this.container.getScope() == memberScope) {
				// Clause in the same scope.
				// Enclosing scope as a topmost container.

				final MemberContainer memberContainer =
						memberScope.getContainer();

				if (!up(this.container, step, memberContainer, null)) {
					return false;
				}
			}
		}

		final MemberClause clause = member.toClause();

		if (clause == null) {
			return notClause();
		}

		this.reused = new ReusedClause(
				this.container.toClause(),
				clause,
				this.reuseContents);

		return true;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return invalidClauseReused();
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return invalidClauseReused();
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {

		final LocalScope enclosing =
				object.getScope().getEnclosingScope().toLocal();

		assert enclosing != null :
			"Enclosing scope is not local: " + enclosing;

		if (!up(object, step, enclosing, null)) {
			return false;
		}

		final PathResolution resolution = dependency.getPath().walk(
				pathResolver(enclosing, User.dummyUser()),
				this);

		return resolution.isResolved();
	}

	@Override
	public boolean object(Step step, Obj object) {
		return notClause();
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		if (this.reused != null) {
			/*assert this.reused.getClause() == result.toMember() :
				"Wrong path resolution: " + result + ", but "
				+ this.reused + " expected";*/
			return true;
		}

		this.container.assertSameScope(result);

		final Clause clause = result.toClause();

		if (clause != null) {
			this.reused = new ReusedClause(
					clause,
					clause.toMember(),
					this.reuseContents);
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

	private boolean notClause() {
		getLogger().notClause(this.location);
		return false;
	}

	private boolean invalidClauseReused() {
		getLogger().error(
				"invalid_clause_reused",
				this.location,
				"Attempt to reuse inaccessible clause");
		return false;
	}

	private boolean unexpectedAbsolutePath() {
		getLogger().error(
				"unexpected_reused_clause_absolute_path",
				this.location,
				"Reused clause should have a relative path");
		return false;
	}

}
