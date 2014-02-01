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
package org.o42a.core.value.link.impl;

import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;


public final class LinkValueConverter implements ValueConverter<KnownLink> {

	private final LinkValueType valueType;

	public LinkValueConverter(LinkValueType valueType) {
		this.valueType = valueType;
	}

	public final LinkValueType getValueType() {
		return this.valueType;
	}

	@Override
	public boolean convertibleFrom(ValueType<?> other) {
		return other.isLink();
	}

	@Override
	public boolean convertibleParameters(
			TypeParameters<KnownLink> destination,
			TypeParameters<?> source) {

		final LinkValueType srcLinkType = source.getValueType().toLinkType();
		final Intrinsics intrinsics = destination.getContext().getIntrinsics();
		final MemberKey destIfaceKey = getValueType().interfaceKey(intrinsics);
		final MemberKey srcIfaceKey = srcLinkType.interfaceKey(intrinsics);

		for (TypeParameter parameter : destination.all()) {

			final MemberKey destKey = parameter.getKey();
			final MemberKey srcKey;

			if (destKey.equals(destIfaceKey)) {
				srcKey = srcIfaceKey;
			} else {
				srcKey = destKey;
			}

			final TypeRef typeRef = source.typeRef(srcKey);

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
	public TypeParameters<KnownLink> convertParameters(
			TypeParameters<?> parameters) {

		final LinkValueType linkType = parameters.getValueType().toLinkType();
		final TypeRef interfaceRef = linkType.interfaceRef(parameters);
		// Rebuild in order to get rid of no longer correct macros.
		final TypeRef newInterfaceRef =
				interfaceRef.rebuildIn(interfaceRef.getScope());

		return getValueType().typeParameters(newInterfaceRef);
	}

}
