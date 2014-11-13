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
import org.o42a.core.ir.object.ObjectIROp;
import org.o42a.core.ir.object.desc.ObjectDescIR;


public class InitObjectFn extends Fn<InitObjectFn> {

	public static final ExtSignature<Void, InitObjectFn> INIT_OBJECT =
			customSignature("InitObjectF", 1)
			.addData("object")
			.addPtr("desc", OBJECT_DESC_TYPE)
			.returnVoid(c -> new InitObjectFn(c));

	private InitObjectFn(FuncCaller<InitObjectFn> caller) {
		super(caller);
	}

	public void initObject(
			Code code,
			ObjectIROp object,
			ObjectDescIR descIR) {
		invoke(
				null,
				code,
				INIT_OBJECT.result(),
				object.toData(null, code),
				descIR.ptr().op(null, code));
	}

}
