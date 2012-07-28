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

import static org.o42a.core.object.link.LinkValueType.LINK;
import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.value.TypeParameters.typeMutability;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.link.impl.LinkByValueAdapter;
import org.o42a.core.object.link.impl.LinkValueAdapter;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


public final class LinkValueStruct
		extends ValueStruct<LinkValueStruct, KnownLink> {

	private final TypeRef typeRef;
	private int linkDepth;

	LinkValueStruct(LinkValueType valueType, TypeRef typeRef) {
		super(valueType, KnownLink.class);
		this.typeRef = typeRef;
	}

	private LinkValueStruct(
			LinkValueStruct prototype,
			LinkValueType valueType,
			TypeRef typeRef) {
		super(valueType, KnownLink.class);
		this.typeRef = typeRef;
		this.linkDepth = prototype.linkDepth;
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
		return new LinkValueStruct(this, valueType, getTypeRef());
	}

	@Override
	public final int getLinkDepth() {
		if (this.linkDepth != 0) {
			return this.linkDepth;
		}
		return this.linkDepth =
				1 + getTypeRef().getValueStruct().getLinkDepth();
	}

	@Override
	public TypeParameters getParameters() {

		final TypeRef typeRef = getTypeRef();

		return typeMutability(
				typeRef,
				typeRef.getRef().distribute(),
				LINK).setTypeRef(typeRef);
	}

	@Override
	public LinkValueStruct setParameters(TypeParameters parameters) {
		if (parameters.getLinkType() != LinkValueType.LINK) {
			parameters.getLogger().error(
					"prohibited_type_mutability",
					parameters.getMutability(),
					"Mutability flag prohibited here. Use a single backquote");
		}

		parameters.assertSameScope(toScoped());

		final TypeRef newTypeRef = parameters.getTypeRef();
		final TypeRef oldTypeRef = getTypeRef();

		if (!newTypeRef.relationTo(oldTypeRef).checkDerived(
				parameters.getLogger())) {
			return this;
		}

		return new LinkValueStruct(this, getValueType(), newTypeRef);
	}

	@Override
	public TypeRelation.Kind relationTo(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return TypeRelation.Kind.INCOMPATIBLE;
		}

		final LinkValueStruct otherLinkStruct = other.toLinkStruct();

		return getTypeRef().relationTo(otherLinkStruct.getTypeRef()).getKind();
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
	public LinkValueStruct prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new LinkValueStruct(this, getValueType(), newTypeRef);
	}

	@Override
	public LinkValueStruct upgradeScope(Scope toScope) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.upgradeScope(toScope);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new LinkValueStruct(this, getValueType(), newTypeRef);
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

		return new LinkValueStruct(this, getValueType(), typeRef);
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
	protected ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {
		if (!adapt || expectedStruct.convertibleFrom(this)) {
			return new LinkValueAdapter(
					ref,
					expectedStruct != null
					? expectedStruct.toLinkStruct()
					: ref.valueStruct(ref.getScope()).toLinkStruct());
		}
		if (expectedStruct.getLinkDepth() - getLinkDepth() == 1) {

			final LinkValueStruct expectedLinkStruct =
					expectedStruct.toLinkStruct();

			return new LinkByValueAdapter(
					adapterRef(ref, expectedLinkStruct.getTypeRef()),
					expectedLinkStruct);
		}

		final Ref adapter = adapterRef(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()));

		return adapter.valueAdapter(expectedStruct, false);
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

		return rescopedStruct.falseValue();
	}

	@Override
	protected void resolveAll(Value<KnownLink> value, FullResolver resolver) {
		getTypeRef().resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
		if (value.getKnowledge().hasCompilerValue()) {
			value.getCompilerValue().resolveAll(resolver);
		}
	}

	@Override
	protected ValueStructIR<LinkValueStruct, KnownLink> createIR(
			Generator generator) {
		return getValueType().structIR(generator, this);
	}

}
