/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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


public final class PathReproduction {

	public static PathReproduction reproducedPath(Path reproducedPath) {
		return new PathReproduction(reproducedPath, null);
	}

	public static PathReproduction outOfClausePath(
			Path reproducedPath,
			Path unchangedPath) {
		assert !unchangedPath.isAbsolute() :
			"Unchanged path should not be absolute after clause left: "
			+ unchangedPath;
		return new PathReproduction(reproducedPath, unchangedPath);
	}

	private final Path reproducedPath;
	private final Path externalPath;

	PathReproduction(Path reproducedPath, Path externalPath) {
		this.reproducedPath = reproducedPath;
		this.externalPath = externalPath;
	}

	public final boolean isOutOfClause() {
		return this.externalPath != null;
	}

	public final Path getReproducedPath() {
		return this.reproducedPath;
	}

	public final Path getExternalPath() {
		return this.externalPath;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (this.externalPath == null) {
			out.append(this.reproducedPath);
		} else {
			out.append(this.reproducedPath);
			out.append("-->");
			out.append(this.externalPath);
		}

		return out.toString();
	}

}
