/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.TypeParameters.typeMutability;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.array.ArrayValueStructIR;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;
import org.o42a.core.value.array.impl.ArrayValueAdapter;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.link.LinkValueType;


public final class ArrayValueStruct
		extends ValueStruct<ArrayValueStruct, Array> {

	private final TypeRef itemTypeRef;
	private ArrayValueStruct constCounterpart;

	ArrayValueStruct(ArrayValueType valueType, TypeRef itemTypeRef) {
		super(valueType, Array.class);
		assert itemTypeRef != null :
			"Array item type not specified";
		this.itemTypeRef = itemTypeRef;
	}

	@Override
	public final ArrayValueType getValueType() {
		return (ArrayValueType) super.getValueType();
	}

	public ArrayValueStruct setVariable(boolean variable) {
		if (isVariable() == variable) {
			return this;
		}
		if (this.constCounterpart != null) {
			return this.constCounterpart;
		}
		return this.constCounterpart = new ArrayValueStruct(
				variable ? ArrayValueType.ARRAY : ArrayValueType.ROW,
				this.itemTypeRef);
	}

	@Override
	public final int getLinkDepth() {
		return 0;
	}

	@Override
	public TypeParameters getParameters() {

		final TypeRef itemTypeRef = getItemTypeRef();

		return typeMutability(
				itemTypeRef,
				itemTypeRef.getRef().distribute(),
				LINK).setTypeRef(itemTypeRef);
	}

	@Override
	public ArrayValueStruct setParameters(TypeParameters parameters) {
		if (parameters.getLinkType() != LinkValueType.LINK) {
			parameters.getLogger().error(
					"prohibited_type_mutability",
					parameters.getMutability(),
					"Mutability flag prohibited here. Use a single backquote");
		}

		parameters.assertSameScope(toScoped());

		final TypeRef newItemTypeRef = parameters.getTypeRef();
		final TypeRef oldItemTypeRef = getItemTypeRef();

		if (!newItemTypeRef.relationTo(oldItemTypeRef)
				.checkDerived(parameters.getLogger())) {
			return this;
		}

		return new ArrayValueStruct(getValueType(), newItemTypeRef);
	}

	public final TypeRef getItemTypeRef() {
		return this.itemTypeRef;
	}

	@Override
	public boolean assignableFrom(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return false;
		}

		final ArrayValueStruct otherArrayStruct = (ArrayValueStruct) other;

		return otherArrayStruct.getItemTypeRef().derivedFrom(getItemTypeRef());
	}

	@Override
	public TypeRelation.Kind relationTo(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return TypeRelation.Kind.INCOMPATIBLE;
		}

		final ArrayValueStruct otherArrayStruct = (ArrayValueStruct) other;

		return getItemTypeRef()
				.relationTo(otherArrayStruct.getItemTypeRef())
				.getKind();
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {

		final ArrayValueStruct otherArrayStruct = other.toArrayStruct();

		if (otherArrayStruct == null) {
			return false;
		}

		return otherArrayStruct.getItemTypeRef().derivedFrom(getItemTypeRef());
	}

	@Override
	public final ScopeInfo toScoped() {
		return getItemTypeRef();
	}

	@Override
	public final LinkValueStruct toLinkStruct() {
		return null;
	}

	@Override
	public final ArrayValueStruct toArrayStruct() {
		return this;
	}

	@Override
	public ArrayValueStruct prefixWith(PrefixPath prefix) {

		final TypeRef oldItemTypeRef = getItemTypeRef();
		final TypeRef newItemTypeRef = oldItemTypeRef.prefixWith(prefix);

		if (oldItemTypeRef == newItemTypeRef) {
			return this;
		}

		return new ArrayValueStruct(getValueType(), newItemTypeRef);
	}

	@Override
	public ArrayValueStruct upgradeScope(Scope toScope) {
		return prefixWith(upgradePrefix(toScoped(), toScope));
	}

	@Override
	public ArrayValueStruct rebuildIn(Scope scope) {

		final TypeRef oldItemTypeRef = getItemTypeRef();
		final TypeRef newItemTypeRef = oldItemTypeRef.rebuildIn(scope);

		if (oldItemTypeRef == newItemTypeRef) {
			return this;
		}

		return new ArrayValueStruct(getValueType(), newItemTypeRef);
	}

	@Override
	public ArrayValueStruct reproduce(Reproducer reproducer) {

		final TypeRef itemTypeRef = getItemTypeRef().reproduce(reproducer);

		if (itemTypeRef == null) {
			return null;
		}

		return new ArrayValueStruct(getValueType(), itemTypeRef);
	}

	@Override
	public void resolveAll(FullResolver resolver) {
		getItemTypeRef().resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (!isVariable()) {
			out.append("Row (`");
		} else {
			out.append("Array (`");
		}

		out.append(this.itemTypeRef).append(')');

		return out.toString();
	}

	@Override
	protected ValueAdapter defaultAdapter(Ref ref, ValueRequest request) {
		if (!request.isTransformAllowed()
				|| request.getExpectedStruct().convertibleFrom(this)) {
			return new ArrayValueAdapter(
					ref,
					request.getExpectedStruct().toArrayStruct());
		}
		return super.defaultAdapter(ref, request);
	}

	@Override
	protected ValueKnowledge valueKnowledge(Array value) {
		return value.getValueKnowledge();
	}

	@Override
	protected Value<Array> prefixValueWith(
			Value<Array> value,
			PrefixPath prefix) {
		if (value.getKnowledge().hasCompilerValue()) {

			final Array array = value.getCompilerValue();

			if (prefix.emptyFor(array)) {
				return value;
			}

			return array.prefixWith(prefix).toValue();
		}

		final ArrayValueStruct initialStruct =
				(ArrayValueStruct) value.getValueStruct();
		final ArrayValueStruct rescopedStruct =
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
	protected void resolveAll(Value<Array> value, FullResolver resolver) {
		getItemTypeRef().resolveAll(resolver.setRefUsage(TYPE_REF_USAGE));
		if (value.getKnowledge().hasCompilerValue()) {

			final ArrayItem[] items =
					value.getCompilerValue().items(resolver.getScope());

			for (ArrayItem item : items) {
				item.resolveAll(resolver);
			}
		}
	}

	@Override
	protected ValueStructIR<ArrayValueStruct, Array> createIR(
			Generator generator) {
		return new ArrayValueStructIR(generator, this);
	}

}