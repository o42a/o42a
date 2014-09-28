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

import static org.o42a.core.ir.object.desc.ObjectIRDesc.OBJECT_DESC_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.desc.ObjectDescIR;


public class AllocateObjectFn extends Fn<AllocateObjectFn> {

	public static final ExtSignature<DataOp, AllocateObjectFn> ALLOCATE_OBJECT =
			customSignature("AllocateObjectF", 1)
			.addPtr("ctr", OBJECT_DESC_TYPE)
			.returnData(c -> new AllocateObjectFn(c));

	private AllocateObjectFn(FuncCaller<AllocateObjectFn> caller) {
		super(caller);
	}

	public DataOp allocateObject(Code code, ObjectDescIR descIR) {
		return invoke(
				null,
				code,
				ALLOCATE_OBJECT.result(),
				descIR.ptr().op(null, code));
	}

}
