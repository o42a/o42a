/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.array;

import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;


final class ArrayTypeRefParamsByItems extends TypeRefParameters {

	private final Ref arrayRef;

	ArrayTypeRefParamsByItems(Ref arrayRef) {
		this.arrayRef = arrayRef;
	}

	@Override
	public final Location getLocation() {
		return this.arrayRef.getLocation();
	}

	@Override
	public Scope getScope() {
		return this.arrayRef.getScope();
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return this.arrayRef.typeParameters(this.arrayRef.getScope())
				.refine(defaultParameters);
	}

	@Override
	public ArrayTypeRefParamsByItems prefixWith(PrefixPath prefix) {

		final Ref arrayRef = this.arrayRef.prefixWith(prefix);

		if (this.arrayRef == arrayRef) {
			return this;
		}

		return new ArrayTypeRefParamsByItems(arrayRef);
	}

	@Override
	public ArrayTypeRefParamsByItems reproduce(Reproducer reproducer) {

		final Ref arrayRef = this.arrayRef.reproduce(reproducer);

		if (arrayRef == null) {
			return null;
		}

		return new ArrayTypeRefParamsByItems(arrayRef);
	}

	@Override
	public String toString() {
		if (this.arrayRef == null) {
			return super.toString();
		}
		return "TypeParameters[" + this.arrayRef + ']';
	}

}
