/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.array.impl.ArrayValueAdapter;
import org.o42a.core.artifact.array.impl.ArrayValueType;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


public final class ArrayValueStruct
		extends ValueStruct<ArrayValueStruct, Array> {

	private final TypeRef itemTypeRef;
	private ArrayValueStruct constCounterpart;

	public ArrayValueStruct(TypeRef itemTypeRef, boolean constant) {
		super(
				constant ? ValueType.CONST_ARRAY : ValueType.ARRAY,
				Array.class);
		this.itemTypeRef = itemTypeRef;
	}

	public final boolean isConstant() {
		return arrayValueType().isConstant();
	}

	public ArrayValueStruct setConstant(boolean constant) {
		if (isConstant() == constant) {
			return this;
		}
		if (this.constCounterpart != null) {
			return this.constCounterpart;
		}
		return this.constCounterpart =
				new ArrayValueStruct(this.itemTypeRef, constant);
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
	public TypeRelation relationTo(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != getValueType()) {
			return TypeRelation.INCOMPATIBLE;
		}

		final ArrayValueStruct otherArrayStruct = (ArrayValueStruct) other;

		return getItemTypeRef().relationTo(
				otherArrayStruct.getItemTypeRef(),
				false);
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {

		final ValueType<?> valueType = other.getValueType();

		if (valueType != ValueType.ARRAY
				&& valueType != ValueType.CONST_ARRAY) {
			return false;
		}

		final ArrayValueStruct otherArrayStruct = (ArrayValueStruct) other;

		return otherArrayStruct.getItemTypeRef().derivedFrom(getItemTypeRef());
	}

	@Override
	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		if (expectedStruct == null || expectedStruct.convertibleFrom(this)) {
			return new ArrayValueAdapter(
					ref,
					(ArrayValueStruct) expectedStruct);
		}

		final Ref adapter = ref.adapt(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()));

		return adapter.valueAdapter(null);
	}

	@Override
	public final ScopeInfo toScoped() {
		return getItemTypeRef();
	}

	@Override
	public ArrayValueStruct prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(toScoped())) {
			return this;
		}
		return new ArrayValueStruct(
				getItemTypeRef().prefixWith(prefix),
				isConstant());
	}

	@Override
	public ArrayValueStruct upgradeScope(Scope toScope) {
		if (toScoped().getScope() == toScope) {
			return this;
		}
		return prefixWith(upgradePrefix(toScoped(), toScope));
	}

	@Override
	public ArrayValueStruct reproduce(Reproducer reproducer) {

		final TypeRef itemTypeRef = getItemTypeRef().reproduce(reproducer);

		if (itemTypeRef == null) {
			return null;
		}

		return new ArrayValueStruct(itemTypeRef, isConstant());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (isConstant()) {
			out.append("Constant array[");
		} else {
			out.append("Array[");
		}

		out.append(this.itemTypeRef).append(']');

		return out.toString();
	}

	@Override
	protected Value<Array> prefixValueWith(
			Value<Array> value,
			PrefixPath prefix) {
		if (value.isDefinite() && !value.isFalse()) {

			final Array array = value.getDefiniteValue();

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
		if (!value.isDefinite()) {
			return rescopedStruct.runtimeValue();
		}
		if (value.isUnknown()) {
			return rescopedStruct.unknownValue();
		}

		return rescopedStruct.falseValue();
	}

	@Override
	protected void resolveAll(Value<Array> value, Resolver resolver) {
		getItemTypeRef().resolveAll(resolver);
		if (value.isDefinite() && !value.isFalse()) {

			final ArrayItem[] items =
					value.getDefiniteValue().items(resolver.getScope());

			for (ArrayItem item : items) {
				item.resolveAll(resolver);
			}
		}
	}

	@Override
	protected ValueStructIR<ArrayValueStruct, Array> createIR(
			Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

	private final ArrayValueType arrayValueType() {
		return (ArrayValueType) getValueType();
	}


}
