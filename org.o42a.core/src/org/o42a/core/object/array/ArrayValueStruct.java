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
package org.o42a.core.object.array;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.impl.ArrayConstantValueDef;
import org.o42a.core.object.array.impl.ArrayValueAdapter;
import org.o42a.core.object.array.impl.ArrayValueStructIR;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


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

	public final boolean isConstant() {
		return arrayValueType().isConstant();
	}

	@Override
	public final ArrayValueType getValueType() {
		return (ArrayValueType) super.getValueType();
	}

	public ArrayValueStruct setConstant(boolean constant) {
		if (isConstant() == constant) {
			return this;
		}
		if (this.constCounterpart != null) {
			return this.constCounterpart;
		}
		return this.constCounterpart = new ArrayValueStruct(
				constant ? ArrayValueType.ROW : ArrayValueType.ARRAY,
				this.itemTypeRef);
	}

	public final TypeRef getItemTypeRef() {
		return this.itemTypeRef;
	}

	@Override
	public final ValueDef constantDef(
			Obj source,
			LocationInfo location,
			Array value) {
		return new ArrayConstantValueDef(source, location, this, value);
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

		final ArrayValueStruct otherArrayStruct = other.toArrayStruct();

		if (otherArrayStruct == null) {
			return false;
		}

		return otherArrayStruct.getItemTypeRef().derivedFrom(getItemTypeRef());
	}

	@Override
	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct,
			boolean adapt) {
		if (!adapt
				|| expectedStruct == null
				|| expectedStruct.convertibleFrom(this)) {
			return new ArrayValueAdapter(
					ref,
					(ArrayValueStruct) expectedStruct);
		}
		return super.defaultAdapter(ref, expectedStruct, adapt);
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
		if (prefix.emptyFor(toScoped())) {
			return this;
		}
		return new ArrayValueStruct(
				getValueType(),
				getItemTypeRef().prefixWith(prefix));
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

		return new ArrayValueStruct(getValueType(), itemTypeRef);
	}

	@Override
	public void resolveAll(Resolver resolver) {
		getItemTypeRef().resolveAll(resolver);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (isConstant()) {
			out.append("Row[");
		} else {
			out.append("Array[");
		}

		out.append(this.itemTypeRef).append(']');

		return out.toString();
	}

	@Override
	protected ValueStruct<ArrayValueStruct, Array> applyParameters(
			TypeParameters parameters) {
		if (parameters.isMutable()) {
			parameters.getLogger().error(
					"prohibited_type_mutability",
					parameters.getMutability(),
					"Mutability flag prohibited here. Use a single backquote");
		}

		parameters.assertSameScope(toScoped());

		final TypeRef newItemTypeRef = parameters.getTypeRef();
		final TypeRef oldItemTypeRef = getItemTypeRef();

		if (!newItemTypeRef.checkDerivedFrom(oldItemTypeRef)) {
			return this;
		}

		return new ArrayValueStruct(getValueType(), newItemTypeRef);
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
		if (value.getKnowledge().getCondition().isUnknown()) {
			return rescopedStruct.unknownValue();
		}

		return rescopedStruct.falseValue();
	}

	@Override
	protected void resolveAll(Value<Array> value, Resolver resolver) {
		getItemTypeRef().resolveAll(resolver);
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

	private final ArrayValueType arrayValueType() {
		return getValueType();
	}

}
