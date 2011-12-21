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
package org.o42a.core.ref.impl.normalizer;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.NormalPath;


public class UnNormalizedPath implements NormalPath {

	private final BoundPath path;

	public UnNormalizedPath(BoundPath path) {
		this.path = path;
	}

	public UnNormalizedPath(Scope origin, BoundPath path) {
		if (path.getOrigin() == origin) {
			this.path = path;
		} else {
			this.path = path.prefixWith(SELF_PATH.toPrefix(origin));
		}
	}

	@Override
	public final boolean isNormalized() {
		return false;
	}

	@Override
	public final Scope getOrigin() {
		return this.path.getOrigin();
	}

	@Override
	public final BoundPath toPath() {
		return this.path;
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return "NormalPath" + this.path;
	}

}
