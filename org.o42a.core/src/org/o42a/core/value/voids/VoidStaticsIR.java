/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.voids;

import static org.o42a.core.ir.IRNames.CONST_ID;
import static org.o42a.core.ir.value.Val.VOID_VAL;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.StaticsIR;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.value.Void;
import org.o42a.util.fn.Init;


final class VoidStaticsIR extends StaticsIR<Void> {

	private final Init<Ptr<ValType.Op>> voidPtr = init(this::allocateVoidVal);

	VoidStaticsIR(ValueTypeIR<Void> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	public Val val(Void value) {
		return VOID_VAL;
	}

	@Override
	public Ptr<ValType.Op> valPtr(Void value) {
		return this.voidPtr.get();
	}

	private Ptr<ValType.Op> allocateVoidVal() {

		final Global<ValType.Op, ValType> global =
				getGenerator()
				.newGlobal()
				.setConstant()
				.dontExport()
				.newInstance(CONST_ID.sub("VOID"), VAL_TYPE, VOID_VAL);

		return global.getPointer();
	}

}
