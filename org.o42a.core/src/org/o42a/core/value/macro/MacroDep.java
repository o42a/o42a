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

import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;


public interface MacroDep<D extends MetaDep> extends RefDep<D> {

	@Override
	default void invalidRef(Ref ref) {
		ref.getLogger().error(
				"invalid_macro_ref",
				ref,
				"Invalid macro reference");
	}

}
