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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;


public final class FldCtrStartFn extends Fn<FldCtrStartFn> {

	public static final ExtSignature<BoolOp, FldCtrStartFn> FLD_CTR_START =
			customSignature("FldCtrStartF", 2)
			.addData("object")
			.addPtr("ctr", FLD_CTR_TYPE)
			.returnBool(c -> new FldCtrStartFn(c));

	private FldCtrStartFn(FuncCaller<FldCtrStartFn> caller) {
		super(caller);
	}

	public final BoolOp call(Code code, DataOp object, FldCtrOp ctr) {
		return invoke(null, code, FLD_CTR_START.result(), object, ctr);
	}

}
