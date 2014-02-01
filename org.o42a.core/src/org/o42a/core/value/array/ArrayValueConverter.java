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
package org.o42a.core.value.array;

import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueConverter;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.impl.DefaultValueConverter;


final class ArrayValueConverter implements ValueConverter<Array> {

	private final ArrayValueType valueType;

	ArrayValueConverter(ArrayValueType valueType) {
		this.valueType = valueType;
	}

	public final ArrayValueType getValueType() {
		return this.valueType;
	}

	@Override
	public boolean convertibleFrom(ValueType<?> other) {
		return other.isArray();
	}

	@Override
	public boolean convertibleParameters(
			TypeParameters<Array> destination,
			TypeParameters<?> source) {
		return DefaultValueConverter.<Array>defaultValueConverter()
				.convertibleParameters(destination, source);
	}

	@Override
	public TypeParameters<Array> convertParameters(
			TypeParameters<?> parameters) {

		final ArrayValueType arrayType =
				parameters.getValueType().toArrayType();
		final TypeRef itemTypeRef = arrayType.itemTypeRef(parameters);
		// Rebuild in order to get rid of no longer correct macros.
		final TypeRef newItemTypeRef =
				itemTypeRef.rebuildIn(itemTypeRef.getScope());

		return getValueType().typeParameters(newItemTypeRef);
	}

}
