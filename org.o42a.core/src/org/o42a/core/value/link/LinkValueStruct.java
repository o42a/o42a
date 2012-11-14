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
package org.o42a.core.value.link;

import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.array.ArrayValueStruct;


public final class LinkValueStruct
		extends ValueStruct<LinkValueStruct, KnownLink> {

	private final TypeRef typeRef;

	LinkValueStruct(LinkValueType valueType, TypeRef typeRef) {
		super(valueType);
		this.typeRef = typeRef;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	@Override
	public final LinkValueType getValueType() {
		return (LinkValueType) super.getValueType();
	}

	public final LinkValueStruct setValueType(LinkValueType valueType) {
		if (valueType == getValueType()) {
			return this;
		}
		return new LinkValueStruct(valueType, getTypeRef());
	}

	@Override
	public final boolean isValid() {
		return getTypeRef().isValid();
	}

	@Override
	public final TypeParameters<KnownLink> getParameters() {
		return getValueType().typeParameters(getTypeRef());
	}

	@Override
	public LinkValueStruct setParameters(TypeParameters<?> parameters) {

		final MemberKey interfaceKey = getValueType().interfaceKey(
				getTypeRef().getContext().getIntrinsics());
		final TypeRef typeRef = parameters.typeRef(interfaceKey);

		if (typeRef == null) {
			return this;
		}

		typeRef.assertSameScope(toScoped());

		if (typeRef.isValid()
				&& !typeRef.relationTo(getTypeRef())
				.checkDerived(parameters.getLogger())) {
			return this;
		}

		return new LinkValueStruct(getValueType(), typeRef);
	}

	@Override
	public LinkValueStruct prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new LinkValueStruct(getValueType(), newTypeRef);
	}

	@Override
	public LinkValueStruct upgradeScope(Scope toScope) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.upgradeScope(toScope);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new LinkValueStruct(getValueType(), newTypeRef);
	}

	@Override
	public LinkValueStruct rebuildIn(Scope scope) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.rebuildIn(scope);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new LinkValueStruct(getValueType(), newTypeRef);
	}

	@Override
	public final ScopeInfo toScoped() {
		return getTypeRef();
	}

	@Override
	public final LinkValueStruct toLinkStruct() {
		return this;
	}

	@Override
	public final ArrayValueStruct toArrayStruct() {
		return null;
	}

	@Override
	public LinkValueStruct reproduce(Reproducer reproducer) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new LinkValueStruct(getValueType(), typeRef);
	}

	@Override
	public void resolveAll(FullResolver resolver) {
		getTypeRef().resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
	}

	@Override
	public String toString() {
		return getValueType() + "(`" + this.typeRef + ')';
	}

	@Override
	protected ValueStructIR<LinkValueStruct, KnownLink> createIR(
			Generator generator) {
		return getValueType().structIR(generator, this);
	}

}
