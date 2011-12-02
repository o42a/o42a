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
package org.o42a.core.ref;

import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathResolver.valuePathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.GroupClause;
import org.o42a.core.ref.impl.resolution.ArtifactResolution;
import org.o42a.core.ref.impl.resolution.ErrorResolution;
import org.o42a.core.ref.impl.resolution.GroupResolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.User;
import org.o42a.util.use.UserInfo;


public class Resolver implements UserInfo, LocationInfo {

	public static ResolverFactory<Resolver> resolverFactory(Scope scope) {
		return new Factory(scope);
	}

	private final Scope scope;
	private final User<?> user;
	private final PathWalker walker;

	protected Resolver(Scope scope, UserInfo user, PathWalker walker) {
		this.scope = scope;
		this.user = user.toUser();
		this.walker = walker;
	}

	@Override
	public final Loggable getLoggable() {
		return this.scope.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.scope.getContext();
	}

	public final boolean isDummy() {
		return this.user.isDummy();
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

	public final CompilerLogger getLogger() {
		return this.scope.getLogger();
	}

	@Override
	public final User<?> toUser() {
		return this.user;
	}

	public Resolver newResolver() {
		if (this.walker == DUMMY_PATH_WALKER) {
			return this;
		}
		return getScope().newResolver(this);
	}

	public final Resolution path(PathResolver resolver, BoundPath path) {

		final PathResolution pathResolution =
				path.walk(resolver.resolveBy(this), getWalker());

		if (!pathResolution.isResolved()) {
			if (pathResolution.isError()) {
				return noResolution(path);
			}
			return null;
		}

		return containerResolution(path, pathResolution.getResult());
	}

	public final PathResolver toPathResolver() {
		return pathResolver(getScope(), this);
	}

	public final PathResolver toFullPathResolver() {
		return fullPathResolver(getScope(), this);
	}

	public final PathResolver toValuePathResolver() {
		return valuePathResolver(getScope(), this);
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}
		if (isDummy()) {
			return "DummyResolver[" + this.scope + ']';
		}
		return "Resolver[" + this.scope + " by " + this.user + ']';
	}

	private final Resolution containerResolution(
			LocationInfo location,
			Container resolved) {
		if (resolved == null) {
			return noResolution(location);
		}

		final Clause clause = resolved.toClause();

		if (clause != null) {
			return clauseResolution(location, clause);
		}

		return artifactResolution(location, resolved.toArtifact());
	}

	private final Resolution artifactResolution(
			LocationInfo location,
			Artifact<?> resolved) {
		if (resolved == null) {
			return noResolution(location);
		}
		return new ArtifactResolution(this, resolved);
	}

	private final Resolution clauseResolution(
			LocationInfo location,
			Clause resolved) {
		if (resolved == null) {
			return noResolution(location);
		}

		final GroupClause group = resolved.toGroupClause();

		if (group != null) {
			return new GroupResolution(this, group);
		}

		return artifactResolution(
				location,
				resolved.toPlainClause().getObject());
	}

	private final Resolution noResolution(LocationInfo location) {
		return new ErrorResolution(this, new Scoped(location, getScope()));
	}

	private static final class Factory extends ResolverFactory<Resolver> {

		Factory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver(
				UserInfo user,
				PathWalker walker) {
			return new Resolver(getScope(), user, walker);
		}

	}

}
