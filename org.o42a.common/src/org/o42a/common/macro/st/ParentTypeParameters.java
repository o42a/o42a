/*
    Compiler Commons
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
package org.o42a.common.macro.st;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Located;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


final class ParentTypeParameters
		extends Located
		implements ObjectTypeParameters {

	ParentTypeParameters(ObjectDefiner definer) {
		super(definer.getField());
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return typeParameters(object).refine(defaultParameters);
	}

	@Override
	public ObjectTypeParameters prefixWith(PrefixPath prefix) {
		throw new UnsupportedOperationException();
	}

	private TypeParameters<?> typeParameters(Obj object) {

		final Scope scope = object.getScope();
		final Scope enclosingScope = scope.getEnclosingScope();
		final Obj parent = enclosingScope.toObject();
		final TypeParameters<?> parentTypeParameters =
				parent.type().getParameters();
		final ValueType<?> parentValueType = parent.type().getValueType();
		final LinkValueType parentLinkType = parentValueType.toLinkType();

		if (parentLinkType != null) {
			if (parentValueType.is(LinkValueType.LINK)) {
				return parentTypeParameters.rescope(scope);
			}

			final MemberKey interfaceKey =
					parentLinkType.interfaceKey(
							parent.getContext().getIntrinsics());

			return LinkValueType.LINK.typeParameters(
					parentTypeParameters.parameter(interfaceKey)
					.getTypeRef()
					.rescope(scope));
		}

		final StaticTypeRef parentValueTypeRef =
				parentValueType.typeRef(enclosingScope, scope)
				.setParameters(parentTypeParameters.rescope(scope));

		return LinkValueType.LINK.typeParameters(parentValueTypeRef);
	}

}
