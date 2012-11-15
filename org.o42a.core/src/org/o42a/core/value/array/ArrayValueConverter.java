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
package org.o42a.core.value.array;

import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;


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

		final ArrayValueType srcArrayType = source.getValueType().toArrayType();
		final Intrinsics intrinsics = destination.getContext().getIntrinsics();
		final MemberKey destItemTypeKey =
				getValueType().itemTypeKey(intrinsics);
		final MemberKey srcItemTypeKey = srcArrayType.itemTypeKey(intrinsics);

		for (TypeParameter parameter : destination.all()) {

			final MemberKey destKey = parameter.getKey();
			final MemberKey srcKey;

			if (destKey.equals(destItemTypeKey)) {
				srcKey = srcItemTypeKey;
			} else {
				srcKey = destKey;
			}

			final TypeRef typeRef = source.typeRef(srcKey);

			if (typeRef == null) {
				return false;
			}
			if (!typeRef.derivedFrom(parameter.getTypeRef())) {
				return false;
			}
		}

		return true;
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
