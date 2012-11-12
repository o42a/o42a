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
package org.o42a.core.ref.impl;

import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeParameter;
import org.o42a.core.ref.type.TypeRef;


public class KnownTypeParameter extends TypeParameter {

	private final TypeRef typeRef;

	public KnownTypeParameter(TypeRef typeRef) {
		super(typeRef, typeRef.getScope());
		assert typeRef != null :
			"Type parameter not specified";
		this.typeRef = typeRef;
	}

	@Override
	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public TypeParameter prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new KnownTypeParameter(newTypeRef);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return this.typeRef.toString();
	}

}
