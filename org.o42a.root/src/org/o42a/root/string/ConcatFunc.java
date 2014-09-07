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
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.value.ValOp;


public class ConcatFunc extends Func<ConcatFunc> {

	public static final ExtSignature<Void, ConcatFunc> CONCAT =
			customSignature("ConcatF", 3)
			.addPtr("output", VAL_TYPE)
			.addPtr("what", VAL_TYPE)
			.addPtr("with", VAL_TYPE)
			.returnVoid(c -> new ConcatFunc(c));

	private ConcatFunc(FuncCaller<ConcatFunc> caller) {
		super(caller);
	}

	public void concat(Code code, ValOp output, ValOp what, ValOp with) {
		invoke(
				null,
				code,
				CONCAT.result(),
				output.ptr(code),
				what.ptr(code),
				with.ptr(code));
	}

}
