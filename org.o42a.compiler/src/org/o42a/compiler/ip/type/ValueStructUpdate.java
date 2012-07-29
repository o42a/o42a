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
package org.o42a.compiler.ip.type;

import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.NestedMetaDep;


final class ValueStructUpdate extends NestedMetaDep {

	ValueStructUpdate(TypeParamMetaDep parent, Meta declaredIn) {
		super(parent, declaredIn);
	}

	@Override
	public MetaDep nestedDep() {
		return null;
	}

	@Override
	protected boolean updateMeta(Meta meta) {

		final TypeParamMetaDep parent =
				(TypeParamMetaDep) parentDep();

		return parent.updateTypeParam(meta);
	}

}
