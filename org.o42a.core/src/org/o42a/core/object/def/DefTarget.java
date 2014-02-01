/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object.def;

import org.o42a.core.ref.Ref;


public final class DefTarget {

	public static final DefTarget NO_DEF_TARGET = new DefTarget(null);
	public static final DefTarget UNKNOWN_DEF_TARGET = new DefTarget(null);

	private final Ref ref;

	public DefTarget(Ref ref) {
		this.ref = ref;
	}

	public final boolean exists() {
		return this != NO_DEF_TARGET;
	}

	public final boolean isUnknown() {
		return this == UNKNOWN_DEF_TARGET;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			if (this == UNKNOWN_DEF_TARGET) {
				return "UnknownTarget";
			}
			return "NoTarget";
		}
		return this.ref.toString();
	}

}
