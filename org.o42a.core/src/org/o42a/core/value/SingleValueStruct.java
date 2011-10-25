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
package org.o42a.core.value;

import static org.o42a.core.value.ValueAdapter.rawValueAdapter;

import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.st.Reproducer;


public abstract class SingleValueStruct<T>
		extends ValueStruct<SingleValueStruct<T>, T> {

	public SingleValueStruct(
			SingleValueType<T> valueType,
			Class<? extends T> valueClass) {
		super(valueType, valueClass);
	}

	@Override
	public SingleValueType<T> getValueType() {
		return (SingleValueType<T>) super.getValueType();
	}

	@Override
	public TypeRelation relationTo(ValueStruct<?, ?> other) {
		if (getValueType() == other.getValueType()) {
			return TypeRelation.SAME;
		}
		return TypeRelation.INCOMPATIBLE;
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {
		return assignableFrom(other);
	}

	@Override
	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		if (expectedStruct == null || expectedStruct.assignableFrom(this)) {
			return rawValueAdapter(ref);
		}

		final Ref adapter = ref.adapt(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()));

		return adapter.valueAdapter(null);
	}

	@Override
	public boolean isScoped() {
		return false;
	}

	@Override
	public final SingleValueStruct<T> rescope(Rescoper rescoper) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> prefixWith(PrefixPath prefix) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> upgradeScope(Scope toScope) {
		return this;
	}

	@Override
	public SingleValueStruct<T> reproduce(Reproducer reproducer) {
		return this;
	}

	@Override
	public String toString() {

		final ValueType<SingleValueStruct<T>> valueType = getValueType();

		if (valueType == null) {
			return super.toString();
		}

		return valueType.toString();
	}

	@Override
	protected void resolveAll(Value<T> value, Resolver resolver) {
	}

}
