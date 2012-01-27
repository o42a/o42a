/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;
import org.o42a.core.value.ValueStruct;


public final class CompilerValue<T> extends Value<T> {

	private final T value;

	public CompilerValue(
			ValueStruct<?, T> valueStruct,
			ValueKnowledge knowledge,
			T value) {
		super(valueStruct, knowledge);
		this.value = value;
	}

	@Override
	public T getCompilerValue() {
		return this.value;
	}

	@Override
	public Val val(Generator generator) {
		return getValueStruct().ir(generator).val(this.value);
	}

	@Override
	public Ptr<ValType.Op> valPtr(Generator generator) {
		return getValueStruct().ir(generator).valPtr(this.value);
	}

	@Override
	public String toString() {
		return valueString(getValueStruct(), this.value);
	}

	static <T> String valueString(ValueStruct<?, T> valueStruct, T value) {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(valueStruct).append(") ");
		out.append(valueStruct.valueString(value));

		return out.toString();
	}

}
