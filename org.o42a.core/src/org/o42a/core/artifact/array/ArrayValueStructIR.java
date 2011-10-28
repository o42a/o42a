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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.*;
import org.o42a.core.ir.value.array.ArrayIRGenerator;


final class ArrayValueStructIR
		extends ValueStructIR<ArrayValueStruct, Array>
		implements ArrayIRGenerator {

	private int idSeq;

	ArrayValueStructIR(Generator generator, ArrayValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public CodeId nextId() {

		final String prefix;

		if (getValueStruct().isConstant()) {
			prefix = "CARRAY_";
		} else {
			prefix = "VARRAY_";
		}

		return getGenerator().id(prefix + (++this.idSeq));
	}

	@Override
	public boolean hasLength() {
		return true;
	}

	@Override
	public Val val(Array value) {
		return value.ir(this).getVal();
	}

	@Override
	public Ptr<ValType.Op> valPtr(Array value) {
		return value.ir(this).getValPtr();
	}

	@Override
	protected void initialize(Code code, ValOp target, ValOp value) {
		store(code, target, value);
		if (value.ptr().getAllocClass().isStatic()) {
			return;
		}
		target.use(code);
	}

	@Override
	protected void assign(Code code, ValOp target, Val value) {
		target.unuse(code);
		initialize(code, target, value);
	}

	@Override
	protected void assign(Code code, ValOp target, ValOp value) {
		target.unuse(code);
		initialize(code, target, value);
	}

}
