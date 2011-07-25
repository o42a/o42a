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

import static org.o42a.core.ref.ResolutionWalker.DUMMY_RESOLUTION_WALKER;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.GroupClause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.impl.resolution.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.*;


public class Resolver implements UserInfo, LocationInfo {

	public static ResolverFactory<Resolver> resolverFactory(Scope scope) {
		return new Factory(scope);
	}

	private final Scope scope;
	private final User user;
	private final ResolutionWalker walker;

	protected Resolver(Scope scope, UserInfo user, ResolutionWalker walker) {
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

	public final ResolutionWalker getWalker() {
		return this.walker;
	}

	public final CompilerLogger getLogger() {
		return this.scope.getLogger();
	}

	@Override
	public final User toUser() {
		return this.user;
	}

	public Resolver newResolver() {
		if (this.walker == DUMMY_RESOLUTION_WALKER) {
			return this;
		}
		return getScope().newResolver(this);
	}

	@Override
	public final UseFlag getUseBy(UseCase useCase) {
		return toUser().getUseBy(useCase);
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	public final Resolution noResolution(LocationInfo location) {
		return new ErrorResolution(new Scoped(location, getScope()));
	}

	public final Resolution path(LocationInfo location, Path path, Scope start) {

		final PathWalker pathWalker = this.walker.path(location, path);

		if (pathWalker == null) {
			return null;
		}

		final Container result = path.walk(location, this, start, pathWalker);

		if (result == null) {
			return null;
		}

		return containerResolution(location, result);
	}

	public final Resolution newObject(ScopeInfo location, Obj object) {
		if (object == null) {
			return noResolution(location);
		}
		if (!this.walker.newObject(location, object)) {
			return null;
		}
		return objectResolution(location, object);
	}

	public final Resolution artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		if (part == null) {
			return noResolution(location);
		}
		if (!this.walker.artifactPart(location, artifact, part)) {
			return null;
		}
		return artifactResolution(location, part);
	}

	public final Resolution staticArtifact(
			LocationInfo location,
			Artifact<?> artifact) {
		if (artifact == null) {
			return noResolution(location);
		}
		if (!this.walker.staticArtifact(location, artifact)) {
			return null;
		}
		return artifactResolution(location, artifact);
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

	private static final class Factory extends ResolverFactory<Resolver> {

		Factory(Scope scope) {
			super(scope);
		}

		@Override
		protected Resolver createResolver(
				UserInfo user,
				ResolutionWalker walker) {
			return new Resolver(getScope(), user, walker);
		}

	}

	final Resolution containerResolution(
			LocationInfo location,
			Container resolved) {
		if (resolved == null) {
			return noResolution(location);
		}

		final LocalScope local = resolved.toLocal();

		if (local != null && local == resolved.getScope().toLocal()) {
			return localResolution(location, local);
		}

		final Clause clause = resolved.toClause();

		if (clause != null) {
			return clauseResolution(location, clause);
		}

		return artifactResolution(location, resolved.toArtifact());
	}

	final Resolution artifactResolution(
			LocationInfo location,
			Artifact<?> resolved) {
		if (resolved == null) {
			return noResolution(location);
		}

		final Obj object = resolved.toObject();

		if (object != null) {
			return new ObjectResolution(object);
		}

		return new ArtifactResolution(resolved);
	}

	final Resolution objectResolution(
			LocationInfo location,
			Obj resolved) {
		if (resolved == null) {
			return noResolution(location);
		}
		return new ObjectResolution(resolved);
	}

	final Resolution localResolution(
			LocationInfo location,
			LocalScope resolved) {
		if (resolved == null) {
			return noResolution(location);
		}
		return new LocalResolution(resolved);
	}

	final Resolution clauseResolution(
			LocationInfo location,
			Clause resolved) {
		if (resolved == null) {
			return noResolution(location);
		}

		final GroupClause group = resolved.toGroupClause();

		if (group != null) {
			return new GroupResolution(group);
		}

		return objectResolution(location, resolved.toPlainClause().getObject());
	}

}
