/*
    Root Object Definition
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
package org.o42a.root.array;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;


final class ArrayOfDuplicatesFn extends Fn<ArrayOfDuplicatesFn> {

	static final
	ExtSignature<Void, ArrayOfDuplicatesFn> ARRAY_OF_DUPLICATES =
			customSignature("ArrayOfDuplicatesF", 3)
			.addPtr("value", VAL_TYPE)
			.addInt32("size")
			.addData("item")
			.returnVoid(c -> new ArrayOfDuplicatesFn(c));

	private ArrayOfDuplicatesFn(FuncCaller<ArrayOfDuplicatesFn> caller) {
		super(caller);
	}

	public void create(CodeDirs dirs, ValOp value, Int32op size, DataOp item) {

		final Block code = dirs.code();

		invoke(
				null,
				code,
				ARRAY_OF_DUPLICATES.result(),
				value.ptr(code),
				size,
				item);

		value.go(code, dirs);
	}

}
