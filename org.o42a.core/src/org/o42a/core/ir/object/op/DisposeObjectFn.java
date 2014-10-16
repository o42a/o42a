/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;


public class DisposeObjectFn extends Fn<DisposeObjectFn> {

	public static final ExtSignature<Void, DisposeObjectFn> DISPOSE_OBJECT =
			customSignature("DisposeObjectF", 1)
			.addPtr("ctr", CtrOp.CTR_TYPE)
			.returnVoid(c -> new DisposeObjectFn(c));

	private DisposeObjectFn(FuncCaller<DisposeObjectFn> caller) {
		super(caller);
	}

	public void dispose(Code code, CtrOp ctr) {
		invoke(null, code, DISPOSE_OBJECT.result(), ctr.ptr(code));
	}

}