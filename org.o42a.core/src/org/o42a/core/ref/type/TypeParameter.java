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
package org.o42a.core.ref.type;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ref.impl.KnownTypeParameter;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public abstract class TypeParameter extends Scoped {

	public static TypeParameter typeParameter(TypeRef typeRef) {
		return new KnownTypeParameter(typeRef);
	}

	public TypeParameter(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	public boolean isValid() {
		return getTypeRef().isValid();
	}

	public abstract TypeRef getTypeRef();

	public abstract TypeParameter prefixWith(PrefixPath prefix);

	public final TypeParameter reproduce(Reproducer reproducer) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return typeParameter(typeRef);
	}

}
