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

import static org.o42a.core.value.link.LinkValueType.LINK;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.link.LinkValueType;


final class ParentTypeParameters implements TypeParametersBuilder {

	private final Scope scope;

	ParentTypeParameters(Scope scope) {
		this.scope = scope;
	}

	@Override
	public ValueStruct<?, ?> valueStructBy(TypeRef typeRef) {
		return valueStruct();
	}

	@Override
	public TypeParameters typeParametersBy(TypeRef typeRef) {
		return typeParameters();
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {
		this.scope.assertCompatibleScope(reproducer.getReproducingScope());
		return new ParentTypeParameters(reproducer.getScope());
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {
		return valueStruct().prefixWith(prefix);
	}

	private TypeParameters typeParameters() {

		final Obj parent = this.scope.toObject();
		final TypeParameters parentTypeParameters =
				parent.type().getParameters();
		final ValueType<?> parentValueType = parent.value().getValueType();
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

	private ValueStruct<?, ?> valueStruct() {

		final Obj parent = this.scope.toObject();
		final ValueStruct<?, ?> parentValueStruct =
				parent.value().getValueStruct();
		final LinkValueStruct parentLinkStruct =
				parentValueStruct.toLinkStruct();

		if (parentLinkStruct != null) {
			// Parent object is link.
			if (parentLinkStruct.getValueType().is(LINK)) {
				// Parent object is link.
				return parentLinkStruct;
			}
			// Construct a link with the same interface.
			return LINK.linkStruct(parentLinkStruct.getTypeRef());
		}

		final StaticTypeRef parentValueTypeRef =
				parentValueStruct.getValueType()
				.typeRef(this.scope, this.scope.getEnclosingScope())
				.setParameters(parentValueStruct)
				.rescope(this.scope);

		// Construct a link.
		return LINK.linkStruct(parentValueTypeRef);
	}

}
