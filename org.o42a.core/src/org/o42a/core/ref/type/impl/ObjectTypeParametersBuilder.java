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
package org.o42a.core.ref.type.impl;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.Location;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;


public final class ObjectTypeParametersBuilder implements ObjectTypeParameters {

	private final TypeRefParameters typeRefParameters;

	public ObjectTypeParametersBuilder(TypeRefParameters typeRefParameters) {
		this.typeRefParameters = typeRefParameters;
	}

	@Override
	public final Location getLocation() {
		return this.typeRefParameters.getLocation();
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return this.typeRefParameters.rescope(object.getScope())
				.refine(defaultParameters);
	}

	@Override
	public ObjectTypeParametersBuilder prefixWith(PrefixPath prefix) {

		final TypeRefParameters typeRefParameters =
				this.typeRefParameters.prefixWith(prefix);

		if (typeRefParameters == this.typeRefParameters) {
			return this;
		}

		return new ObjectTypeParametersBuilder(typeRefParameters);
	}

	@Override
	public String toString() {
		if (this.typeRefParameters == null) {
			return super.toString();
		}
		return this.typeRefParameters.toString();
	}

}
