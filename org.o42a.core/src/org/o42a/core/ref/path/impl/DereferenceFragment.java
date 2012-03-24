/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;


public final class DereferenceFragment extends PathFragment {

	public static final DereferenceFragment DEREFERENCE_FRAGMENT =
			new DereferenceFragment();

	private DereferenceFragment() {
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		if (!canDereference(start)) {
			return SELF_PATH;
		}
		return SELF_PATH.dereference();
	}

	@Override
	public String toString() {
		return "->";
	}

	static boolean canDereference(Scope start) {

		final Obj object = start.toObject();

		if (object == null) {
			return false;
		}

		return object.value().getValueType().toLinkType() != null;
	}

}
