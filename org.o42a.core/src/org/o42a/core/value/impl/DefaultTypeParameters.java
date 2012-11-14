/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.core.value.ValueStruct;


public final class DefaultTypeParameters implements TypeParametersBuilder {

	public static final DefaultTypeParameters INSTANCE =
			new DefaultTypeParameters();

	private DefaultTypeParameters() {
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {
		return this;
	}

	@Override
	public ValueStruct<?, ?> valueStructBy(TypeRef typeRef) {
		return typeRef.defaultValueStruct();
	}

	@Override
	public TypeParameters<?> typeParametersBy(TypeRef typeRef) {
		return typeRef.defaultParameters();
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {
		return this;
	}

}
