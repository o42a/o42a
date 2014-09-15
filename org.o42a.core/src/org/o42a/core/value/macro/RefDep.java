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
package org.o42a.core.value.macro;

import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;


public interface RefDep<D extends MetaDep> {

	default D buildDep(Ref ref, PathTemplate template) {
		if (ref.isStatic()) {
			return null;
		}

		final RefDepBuilder<D> builder =
				new RefDepBuilder<>(this, ref, template);

		return builder.buildDep();
	}

	D newDep(Meta meta, Ref ref, PathTemplate template);

	void setParentDep(D dep, MetaDep parentDep);

	default void invalidRef(Ref ref) {
		ref.getLogger().error(
				"invalid_ref_meta_dep",
				ref,
				"Invalid meta-reference");
	}

}
