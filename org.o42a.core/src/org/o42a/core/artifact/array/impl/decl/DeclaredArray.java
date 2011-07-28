/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl.decl;

import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.array.ArrayTypeRef;
import org.o42a.core.ref.type.TypeRef;


final class DeclaredArray extends Array {

	private final ArrayFieldVariant variant;
	private boolean built;

	DeclaredArray(ArrayFieldVariant variant) {
		super(variant.getField());
		this.variant = variant;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.variant.validate();
	}

	@Override
	public String toString() {
		return this.variant.getArrayField().toString();
	}

	@Override
	protected ArrayTypeRef buildTypeRef() {
		return null;
	}

	@Override
	protected TypeRef buildItemTypeRef() {
		build();
		return this.variant.getItemTypeRef();
	}

	@Override
	protected ArrayInitializer buildInitializer() {
		build();
		return this.variant.getInitializer();
	}

	private void build() {
		if (this.built) {
			return;
		}
		this.built = true;
		this.variant.build(
				null,
				this.variant.getArrayField().declaredItemTypeRef());
	}

}
