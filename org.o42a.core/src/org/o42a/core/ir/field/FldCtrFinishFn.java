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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.field.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.ObjectIRLock.OBJECT_IR_LOCK;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.ObjOp;


public final class FldCtrFinishFn extends Fn<FldCtrFinishFn> {

	public static final ExtSignature<Void, FldCtrFinishFn> FLD_CTR_FINISH =
			customSignature("FldCtrFinishF", 2)
			.addData("object")
			.addPtr("lock", OBJECT_IR_LOCK)
			.addPtr("ctr", FLD_CTR_TYPE)
			.returnVoid(c -> new FldCtrFinishFn(c));

	private FldCtrFinishFn(FuncCaller<FldCtrFinishFn> caller) {
		super(caller);
	}

	public final void call(Code code, ObjOp object, FldCtrOp ctr) {
		invoke(
				null,
				code,
				FLD_CTR_FINISH.result(),
				object.toData(null, code),
				object.lock(code),
				ctr);
	}

}
