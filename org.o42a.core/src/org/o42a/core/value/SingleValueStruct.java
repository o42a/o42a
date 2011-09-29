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

import static org.o42a.core.def.Def.sourceOf;

import org.o42a.core.def.CondDef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.def.impl.RefCondDef;
import org.o42a.core.def.impl.RefValueDef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;


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
	public boolean assignableFrom(ValueStruct<?, ?> other) {
		return getValueType() == other.getValueType();
	}

	@Override
	public SingleValueStruct<T> rescope(Rescoper rescoper) {
		return this;
	}

	@Override
	public ValueDef valueDef(Ref ref) {
		return new RefValueDef(ref);
	}

	@Override
	public CondDef condDef(Ref ref) {
		return new RefCondDef(sourceOf(ref), ref);
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
	protected Value<T> rescope(Value<T> value, Rescoper rescoper) {
		return value;
	}

	@Override
	protected Value<T> resolveAll(Value<T> value, Resolver resolver) {
		// TODO Auto-generated method stub
		return null;
	}

}
