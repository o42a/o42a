/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.core.ContainerInfo;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class PrefixPath {

	public static PrefixPath emptyPrefix(Scope finalScope) {
		return Path.SELF_PATH.toPrefix(finalScope);
	}

	public static PrefixPath upgradePrefix(ScopeInfo scoped, Scope toScope) {
		scoped.assertCompatible(toScope);
		return Path.SELF_PATH.toPrefix(toScope);
	}

	private final Scope start;
	private final Path prefix;
	private BoundPath boundPath;

	PrefixPath(Scope start, Path prefix) {
		this.start = start;
		this.prefix = prefix;
	}

	PrefixPath(Scope start, Path prefix, BoundPath boundPath) {
		this.start = start;
		this.prefix = prefix;
		this.boundPath = boundPath;
	}

	public final Scope getStart() {
		return this.start;
	}

	public final Path getPrefix() {
		return this.prefix;
	}

	public final boolean isEmpty() {
		return getPrefix().isSelf();
	}

	public final boolean emptyFor(ScopeInfo scoped) {
		return isEmpty() && getStart().is(scoped.getScope());
	}

	public final PrefixPath and(PrefixPath other) {
		if (other.isEmpty()) {
			if (other.getStart().is(getStart())) {
				return this;
			}
			getStart().assertCompatible(other.getStart());
		}

		final Path newPath = other.getPrefix().append(getPrefix());

		return newPath.toPrefix(other.getStart());
	}

	public final BoundPath bind(LocationInfo location) {
		return getPrefix().bind(location, getStart());
	}

	public final Ref target(ContainerInfo location) {
		return bind(location)
				.target(location.distributeIn(getStart().getContainer()));
	}

	public Scope rescope(Scope scope) {

		final PathResolution found = getBoundPath().resolve(
				scope.resolver().toPathResolver());

		return found.isResolved() ? found.getResult().getScope() : null;
	}

	public Resolver rescope(Resolver resolver) {

		final BoundPath path = getBoundPath();
		final PathResolution found = path.walk(
				resolver.toPathResolver(),
				resolver.getWalker());

		if (!found.isResolved()) {
			return null;
		}

		return found.getResult().getScope().walkingResolver(resolver);
	}

	public PrefixPath reproduce(Reproducer reproducer) {

		final Scope scope = reproducer.getScope();
		final BoundPath boundPath = getBoundPath();

		if (boundPath.isSelf() || boundPath.isAbsolute()) {
			return boundPath.toPrefix(reproducer.getScope());
		}

		final PathReproducer pathReproducer = boundPath.reproducer(reproducer);
		final PathReproduction pathReproduction =
				pathReproducer.reproducePath();

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return pathReproduction.getExternalPath().toPrefix(scope);
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startWithPrefix(reproducer, pathReproduction);
		}

		final Path reproducedPart = pathReproduction.getReproducedPath();

		return startWithPrefix(reproducer, pathReproduction)
				.append(reproducedPart);
	}

	public HostOp write(CodeDirs dirs, HostOp host) {
		if (getPrefix().isSelf()) {
			return host;
		}

		final CodeDirs subDirs =
				dirs.begin("rescope_by_path", "Resccope to " + this.prefix);
		final HostOp result = getBoundPath().op(subDirs, host);

		subDirs.done();

		return result;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.start.hashCode();
		result = prime * result + this.prefix.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final PrefixPath other = (PrefixPath) obj;

		if (this.start != other.start) {
			return false;
		}
		if (!this.prefix.equals(other.prefix)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.prefix == null) {
			return super.toString();
		}
		return this.prefix.toString(this.start, this.prefix.getSteps().length);
	}

	final BoundPath getBoundPath() {
		if (this.boundPath != null) {
			return this.boundPath;
		}
		return this.boundPath = bind(getStart());
	}

	private final PrefixPath append(Path path) {
		if (path.isSelf()) {
			return this;
		}
		return getPrefix().append(path).toPrefix(getStart());
	}

	private PrefixPath startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction) {

		final PrefixPath phrasePrefix =
				reproducer.getPhrasePrefix()
				.getPath()
				.toPrefix(reproducer.getPhrasePrefix().getScope());
		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix;
		}

		return externalPath
				.toPrefix(phrasePrefix.rescope(phrasePrefix.getStart()))
				.append(phrasePrefix.getPrefix());
	}

}
