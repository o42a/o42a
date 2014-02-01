/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ref.type.impl;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.LocationInfo;


public final class DefaultTypeRef extends TypeRef {

	public DefaultTypeRef(Ref ref, TypeRefParameters typeParameters) {
		super(ref, typeParameters);
	}

	@Override
	public boolean isStatic() {
		return getRef().isStatic();
	}

	@Override
	public final Ref getIntactRef() {
		return getRef();
	}

	@Override
	public TypeRef setLocation(LocationInfo location) {
		return new DefaultTypeRef(
				getRef().setLocation(location),
				copyParameters());
	}

	@Override
	protected TypeRef create(
			Ref intactRef,
			Ref ref,
			TypeRefParameters parameters) {
		assert intactRef == ref :
			intactRef + " should be the same as " + intactRef;
		return new DefaultTypeRef(ref, parameters);
	}

}
