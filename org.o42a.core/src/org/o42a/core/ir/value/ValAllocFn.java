/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.op.ValDirs;


public final class ValAllocFn extends Fn<ValAllocFn> {

	public static final ExtSignature<AnyOp, ValAllocFn> VAL_ALLOC =
			customSignature("ValAllocF", 2)
			.addPtr("value", VAL_TYPE)
			.addInt32("size")
			.returnAny(c -> new ValAllocFn(c));

	private ValAllocFn(FuncCaller<ValAllocFn> caller) {
		super(caller);
	}

	public final AnyOp allocate(ValDirs dirs, int size) {
		return allocate(dirs, dirs.code().int32(size));
	}

	public final AnyOp allocate(ValDirs dirs, Int32op size) {

		final Block code = dirs.code();
		final ValOp value = dirs.value();
		final AnyOp result = invoke(
				null,
				code,
				VAL_ALLOC.result(),
				value.ptr(code),
				size);

		value.go(dirs.code(), dirs.dirs());

		return result;
	}

}
