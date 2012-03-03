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
package org.o42a.core.object.link;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.impl.LinkConstantValueDef;
import org.o42a.core.object.link.impl.LinkValueAdapter;
import org.o42a.core.object.link.impl.LinkValueStructIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


public final class LinkValueStruct
		extends ValueStruct<LinkValueStruct, KnownLink> {

	private final TypeRef typeRef;

	LinkValueStruct(LinkValueType valueType, TypeRef typeRef) {
		super(valueType, KnownLink.class);
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
		return valueType.linkStruct(getTypeRef());
	}

	@Override
	public ValueDef constantDef(
			Obj source,
			LocationInfo location,
			KnownLink value) {
		return new LinkConstantValueDef(source, location, this, value);
	}

	@Override
	public TypeRelation relationTo(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return TypeRelation.INCOMPATIBLE;
		}

		final LinkValueStruct otherLinkStruct = (LinkValueStruct) other;

		return getTypeRef().relationTo(otherLinkStruct.getTypeRef(), false);
	}

	@Override
	public boolean assignableFrom(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return false;
		}

		final LinkValueStruct otherLinkStruct = other.toLinkStruct();

		return otherLinkStruct.getTypeRef().derivedFrom(getTypeRef());
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {

		final LinkValueStruct otherLinkStruct = other.toLinkStruct();

		if (otherLinkStruct == null) {
			return false;
		}

		return otherLinkStruct.getTypeRef().derivedFrom(getTypeRef());
	}

	@Override
	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		if (expectedStruct == null || expectedStruct.convertibleFrom(this)) {
			return new LinkValueAdapter(
					ref,
					(LinkValueStruct) expectedStruct);
		}

		final Ref adapter = ref.adapt(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()));

		return adapter.valueAdapter(null);
	}

	@Override
	public LinkValueStruct prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return getValueType().linkStruct(newTypeRef);
	}

	@Override
	public LinkValueStruct upgradeScope(Scope toScope) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.upgradeScope(toScope);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return getValueType().linkStruct(newTypeRef);
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
	public LinkValueStruct reproduce(Reproducer reproducer) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return getValueType().linkStruct(typeRef);
	}

	@Override
	public String toString() {
		return getValueType() + "(`" + this.typeRef + ')';
	}

	@Override
	protected ValueStruct<LinkValueStruct, KnownLink> applyParameters(
			TypeParameters parameters) {
		if (parameters.isMutable()) {
			parameters.getLogger().error(
					"prohibited_type_mutability",
					parameters.getMutability(),
					"Mutability flag prohibited here. Use a single backquote");
		}

		parameters.assertSameScope(toScoped());

		final TypeRef newTypeRef = parameters.getTypeRef();
		final TypeRef oldTypeRef = getTypeRef();

		if (!newTypeRef.checkDerivedFrom(oldTypeRef)) {
			return this;
		}

		return getValueType().linkStruct(newTypeRef);
	}

	@Override
	protected ValueKnowledge valueKnowledge(KnownLink value) {
		return value.getKnowledge();
	}

	@Override
	protected Value<KnownLink> prefixValueWith(
			Value<KnownLink> value,
			PrefixPath prefix) {
		if (value.getKnowledge().hasCompilerValue()) {

			final KnownLink link = value.getCompilerValue();

			if (prefix.emptyFor(link)) {
				return value;
			}

			return link.prefixWith(prefix).toValue();
		}

		final LinkValueStruct initialStruct =
				(LinkValueStruct) value.getValueStruct();
		final LinkValueStruct rescopedStruct =
				initialStruct.prefixWith(prefix);

		if (initialStruct == rescopedStruct) {
			return value;
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return rescopedStruct.runtimeValue();
		}
		if (value.getKnowledge().getCondition().isUnknown()) {
			return rescopedStruct.unknownValue();
		}

		return rescopedStruct.falseValue();
	}

	@Override
	protected void resolveAll(Value<KnownLink> value, Resolver resolver) {
		getTypeRef().resolveAll(resolver);
		if (value.getKnowledge().hasCompilerValue()) {
			value.resolveAll(resolver);
		}
	}

	@Override
	protected ValueStructIR<LinkValueStruct, KnownLink> createIR(
			Generator generator) {
		return new LinkValueStructIR(generator, this);
	}

}
