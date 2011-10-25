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
package org.o42a.core.ref.path;

import org.o42a.core.PlaceInfo;
import org.o42a.core.Rescoper;
import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.PathRescoper;
import org.o42a.core.source.LocationInfo;


public final class PrefixPath {

	private final Scope start;
	private final Path prefix;

	PrefixPath(Scope start, Path prefix) {
		this.start = start;
		this.prefix = prefix;
	}

	public final Scope getStart() {
		return this.start;
	}

	public final Path getPrefix() {
		return this.prefix;
	}

	public final PrefixPath append(Path path) {
		return getPrefix().append(path).toPrefix(getStart());
	}

	public final PrefixPath append(BoundPath path) {
		return append(path.getRawPath());
	}

	public final BoundPath bind(LocationInfo location) {
		return getPrefix().bind(location, getStart());
	}

	public final Ref target(PlaceInfo location) {
		return bind(location)
				.target(location.distributeIn(getStart().getContainer()));
	}

	public final Rescoper toRescoper() {
		if (!this.prefix.isStatic() && this.prefix.isSelf()) {
			return Rescoper.transparentRescoper(getStart());
		}
		return new PathRescoper(this);
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

}
