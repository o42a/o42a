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
package org.o42a.core.value.impl;

import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.*;


public class DefaultValueConverter<T> implements ValueConverter<T> {

	@SuppressWarnings("rawtypes")
	private static final DefaultValueConverter<?> DEFAULT_VALUE_CONVERTER =
			new DefaultValueConverter();

	@SuppressWarnings("unchecked")
	public static <T> DefaultValueConverter<T> defaultValueConverter() {
		return (DefaultValueConverter<T>) DEFAULT_VALUE_CONVERTER;
	}

	private DefaultValueConverter() {
	}

	@Override
	public boolean convertibleFrom(ValueType<?> other) {
		return false;
	}

	@Override
	public boolean convertibleParameters(
			TypeParameters<T> destination,
			TypeParameters<?> source) {
		for (TypeParameter parameter : destination.all()) {

			final MemberKey key = parameter.getKey();
			final TypeRef typeRef = source.typeRef(key);

			if (typeRef == null) {
				continue;
			}
			if (!typeRef.derivedFrom(parameter.getTypeRef())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public TypeParameters<T> convertParameters(TypeParameters<?> parameters) {
		throw new UnsupportedOperationException();
	}

}
