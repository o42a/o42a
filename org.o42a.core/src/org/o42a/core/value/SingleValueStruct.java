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

import org.o42a.core.def.Rescoper;


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
	public String toString() {

		final ValueType<SingleValueStruct<T>> valueType = getValueType();

		if (valueType == null) {
			return super.toString();
		}

		return valueType.toString();
	}

}
