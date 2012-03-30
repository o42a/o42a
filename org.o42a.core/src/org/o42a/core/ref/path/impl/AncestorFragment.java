/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;


public class AncestorFragment extends PathFragment {

	public static final AncestorFragment ANCESTOR_FRAGMENT =
			new AncestorFragment();

	private AncestorFragment() {
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Obj object = start.toObject();

		assert object != null :
			"Only object may have an ancestor: " + start;

		final Link dereferencedLink = object.getDereferencedLink();

		if (dereferencedLink != null) {
			return ancestor(start, dereferencedLink.getTypeRef());
		}

		return ancestor(start, object.type().getAncestor());
	}

	@Override
	public String toString() {
		return "^^";
	}

	private Path ancestor(Scope objectScope, TypeRef ancestor) {
		if (ancestor == null) {
			return ValueType.VOID.path(
					objectScope.getContext().getIntrinsics());
		}
		return objectScope.getEnclosingScopePath()
				.append(ancestor.getPath().getPath());
	}

}
