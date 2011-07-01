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
package org.o42a.core.ref.path;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;


final class PathRescoper extends Rescoper {

	private final Path path;

	PathRescoper(Path path, Scope finalScope) {
		super(finalScope);
		this.path = path.rebuild();
	}

	@Override
	public final Path getPath() {
		return this.path;
	}

	@Override
	public Scope rescope(Scope scope) {

		final Container found =
			this.path.resolve(scope, scope.dummyResolver(), scope);

		return found != null ? found.getScope() : null;
	}

	@Override
	public Resolver rescope(Resolver resolver) {

		final Container found =
				this.path.resolve(resolver, resolver, resolver.getScope());

		if (found == null) {
			return null;
		}

		return found.getScope().newResolver(resolver);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	public Rescoper and(Rescoper other) {
		if (other instanceof PathRescoper) {

			final PathRescoper pathRescoper = (PathRescoper) other;
			final Path newPath = pathRescoper.path.append(this.path);

			return new PathRescoper(newPath, other.getFinalScope());
		}

		return super.and(other);
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {

		final Scope scope = reproducer.getScope();
		final PathReproduction pathReproduction =
			this.path.reproduce(location, reproducer);

		if (pathReproduction == null) {
			return null;
		}

		final PathRescoper reproducedPart = new PathRescoper(
				pathReproduction.getReproducedPath(),
				scope);

		if (!pathReproduction.isOutOfClause()
				|| pathReproduction.isUnchanged()) {
			return reproducedPart;
		}

		final Rescoper phraseRescoper =
			reproducer.getPhrasePrefix().toRescoper();
		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phraseRescoper.and(reproducedPart);
		}

		return externalPath.rescoper(
				phraseRescoper.rescope(phraseRescoper.getFinalScope()))
				.and(phraseRescoper)
				.and(reproducedPart);
	}

	@Override
	public void resolveAll(Resolver resolver) {
		this.path.resolve(resolver, resolver, resolver.getScope());
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {

		final CodeDirs subDirs =
				dirs.begin("rescope_by_path", "Resccope to " + this.path);
		final HostOp result = this.path.write(subDirs, host);

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

}
