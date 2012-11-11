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
package org.o42a.core.value;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class TypeParameters extends Placed implements ValueStructFinder {

	private TypeRef typeRef;

	public TypeParameters(LocationInfo location, Distributor distributor) {
		super(location, distributor);
		this.typeRef = null;
	}

	private TypeParameters(
			LocationInfo location,
			Distributor distributor,
			TypeRef typeRef) {
		super(location, distributor);
		this.typeRef = typeRef;
		assertSameScope(typeRef);
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final TypeParameters setTypeRef(TypeRef typeRef) {
		return new TypeParameters(this, distribute(), typeRef);
	}

	@Override
	public final ValueStruct<?, ?> valueStructBy(
			ValueStruct<?, ?> defaultStruct) {
		return defaultStruct.setParameters(this);
	}

	@Override
	public final TypeParameters prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();

		if (oldTypeRef == null) {
			if (getScope().is(prefix.getStart())) {
				return this;
			}
			return new TypeParameters(
					this,
					distributeIn(prefix.getStart().getContainer()));
		}

		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new TypeParameters(
				this,
				distributeIn(prefix.getStart().getContainer()),
				newTypeRef);
	}

	@Override
	public ValueStructFinder reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final TypeRef oldTypeRef = getTypeRef();

		if (oldTypeRef == null) {
			return new TypeParameters(this, reproducer.distribute());
		}

		final TypeRef newTypeRef = oldTypeRef.reproduce(reproducer);

		if (newTypeRef == null) {
			return null;
		}

		return new TypeParameters(
				this,
				reproducer.distribute(),
				newTypeRef);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return "(`*)";
		}
		return "(`" + this.typeRef + ")";
	}

}
