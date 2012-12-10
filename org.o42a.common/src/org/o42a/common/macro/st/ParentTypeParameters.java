/*
    Modules Commons
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
package org.o42a.common.macro.st;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


final class ParentTypeParameters
		extends Location
		implements TypeParametersBuilder {

	private final Scope scope;

	ParentTypeParameters(Scope scope) {
		super(scope);
		this.scope = scope;
	}

	@Override
	public boolean isDefaultTypeParameters() {
		return false;
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return typeParameters().refine(defaultParameters);
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {
		this.scope.assertCompatibleScope(reproducer.getReproducingScope());
		return new ParentTypeParameters(reproducer.getScope());
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {
		return typeParameters().prefixWith(prefix);
	}

	private TypeParameters<?> typeParameters() {

		final Obj parent = this.scope.toObject();
		final TypeParameters<?> parentTypeParameters =
				parent.type().getParameters();
		final ValueType<?> parentValueType = parent.type().getValueType();
		final LinkValueType parentLinkType = parentValueType.toLinkType();

		if (parentLinkType != null) {
			if (parentValueType.is(LinkValueType.LINK)) {
				return parentTypeParameters;
			}

			final MemberKey interfaceKey =
					parentLinkType.interfaceKey(
							parent.getContext().getIntrinsics());

			return LinkValueType.LINK.typeParameters(
					parentTypeParameters.parameter(interfaceKey).getTypeRef());
		}

		final StaticTypeRef parentValueTypeRef =
				parentValueType.typeRef(
						this.scope,
						this.scope.getEnclosingScope())
				.setParameters(parentTypeParameters)
				.rescope(this.scope);

		return LinkValueType.LINK.typeParameters(parentValueTypeRef);
	}

}
