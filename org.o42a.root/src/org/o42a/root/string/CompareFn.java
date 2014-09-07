/*
    Root Object Definition
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
package org.o42a.root.string;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.value.ValOp;


public class CompareFn extends Fn<CompareFn> {

	public static final ExtSignature<Int64op, CompareFn> COMPARE =
			customSignature("CompareF", 2)
			.addPtr("what", VAL_TYPE)
			.addPtr("with", VAL_TYPE)
			.returnInt64(c -> new CompareFn(c));

	private CompareFn(FuncCaller<CompareFn> caller) {
		super(caller);
	}

	public Int64op compare(Code code, ValOp what, ValOp with) {
		return invoke(
				null,
				code,
				COMPARE.result(),
				what.ptr(code),
				with.ptr(code));
	}

}
