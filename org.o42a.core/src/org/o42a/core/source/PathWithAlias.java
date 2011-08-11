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
package org.o42a.core.source;

import org.o42a.core.ref.Ref;


public final class PathWithAlias {

	private final Ref path;
	private final String alias;

	public PathWithAlias(Ref path, String alias) {
		this.path = path;
		this.alias = alias;
	}

	public final Ref getPath() {
		return this.path;
	}

	public final String getAlias() {
		return this.alias;
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		if (this.alias == null) {
			return this.path.toString();
		}
		return this.path + " as " + this.alias;
	}

}
