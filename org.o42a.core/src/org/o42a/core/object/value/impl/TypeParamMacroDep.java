/*
    Compiler Core
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
package org.o42a.core.object.value.impl;

import org.o42a.core.object.Meta;
import org.o42a.core.object.macro.MacroDep;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.MetaKey;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;


public final class TypeParamMacroDep
		extends MacroDep<TypeParamMetaDep>
		implements MetaKey {

	private final Nesting nesting;
	private final int depth;

	public TypeParamMacroDep(Nesting nesting, int depth) {
		this.nesting = nesting;
		this.depth = depth;
	}

	public final Nesting getNesting() {
		return this.nesting;
	}

	public final int getDepth() {
		return this.depth;
	}

	@Override
	public TypeParamMetaDep newDep(Meta meta, Ref ref) {
		return new TypeParamMetaDep(meta, this, ref);
	}

	@Override
	public void setParentDep(TypeParamMetaDep dep, MetaDep parentDep) {
		dep.setParentDep(parentDep);
	}

}
