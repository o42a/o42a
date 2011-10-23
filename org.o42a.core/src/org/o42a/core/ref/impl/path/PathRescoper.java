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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathResolver.fullPathResolver;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.Reproducer;


public final class PathRescoper extends Rescoper {

	private final BoundPath path;

	public PathRescoper(BoundPath path) {
		super(path.getOrigin());
		this.path = path;
	}

	@Override
	public boolean isStatic() {
		return this.path.isStatic();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	@Override
	public Scope rescope(Scope scope) {

		final PathResolution found = this.path.resolve(
				pathResolver(scope.dummyResolver()),
				scope);

		return found.isResolved() ? found.getResult().getScope() : null;
	}

	@Override
	public Resolver rescope(Resolver resolver) {

		final PathWalker pathWalker =
				resolver.getWalker().path(this.path);

		if (pathWalker == null) {
			return null;
		}

		final PathResolution found = this.path.walk(
				pathResolver(resolver),
				resolver.getScope(),
				pathWalker);

		if (!found.isResolved()) {
			return null;
		}

		return found.getResult().getScope().walkingResolver(resolver);
	}

	@Override
	public Ref rescopeRef(Ref ref) {

		final BoundPath newPath = getPath().append(ref.getPath());

		return newPath.target(
				ref.distributeIn(getFinalScope().getContainer()));
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	public Rescoper and(Rescoper other) {
		if (other instanceof PathRescoper) {

			final PathRescoper pathRescoper = (PathRescoper) other;
			final Path newPath = pathRescoper.getPath().getRawPath().append(
					getPath().getRawPath());

			return new PathRescoper(
					newPath.bind(this.path, other.getFinalScope()));
		}

		return super.and(other);
	}

	@Override
	public Rescoper reproduce(Reproducer reproducer) {

		final Scope scope = reproducer.getScope();
		final PathReproduction pathReproduction =
				getPath().reproduce(reproducer);

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return new PathRescoper(
						pathReproduction.getExternalPath().bind(
								this.path,
								scope));
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startWithPrefix(reproducer, pathReproduction);
		}

		final PathRescoper reproducedPart = new PathRescoper(
				pathReproduction.getReproducedPath().bind(this.path, scope));

		return startWithPrefix(reproducer, pathReproduction)
				.and(reproducedPart);
	}

	@Override
	public void resolveAll(Resolver resolver) {
		this.path.resolve(fullPathResolver(resolver), resolver.getScope());
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {

		final CodeDirs subDirs =
				dirs.begin("rescope_by_path", "Resccope to " + this.path);
		final HostOp result = this.path.op(subDirs, host);

		subDirs.end();

		return result;
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
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

		final PathRescoper other = (PathRescoper) obj;

		if (getFinalScope() != other.getFinalScope()) {
			return false;
		}
		if (!this.path.equals(other.path)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "RescopeTo[" + this.path + ']';
	}

	private Rescoper startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction) {

		final Rescoper phraseRescoper =
				reproducer.getPhrasePrefix().materialize().toRescoper();
		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phraseRescoper;
		}

		return externalPath.bind(
				this.path,
				phraseRescoper.rescope(phraseRescoper.getFinalScope()))
				.rescoper()
				.and(phraseRescoper);
	}

}
