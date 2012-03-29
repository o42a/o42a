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
package org.o42a.core.object.array.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.array.ArrayValueType;


public final class ArrayValueStructIR
		extends ValueStructIR<ArrayValueStruct, Array> {

	public ArrayValueStructIR(
			Generator generator,
			ArrayValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	public ArrayValueTypeIR getValueTypeIR() {

		final ArrayValueType valueType = (ArrayValueType) getValueType();

		return valueType.ir(getGenerator());
	}

	@Override
	public boolean hasLength() {
		return true;
	}

	@Override
	public Val val(Array value) {
		return value.ir(getValueTypeIR()).getVal();
	}

	@Override
	public Ptr<ValType.Op> valPtr(Array value) {
		return value.ir(getValueTypeIR()).getValPtr();
	}

}
