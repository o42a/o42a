/*
    Console Module
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.lib.console.impl;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.lib.console.ConsoleModule.CONSOLE_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.value.ValOp;


public final class PrintFn extends Fn<PrintFn> {

	public static final ExtSignature<Void, PrintFn> PRINT =
			customSignature(CONSOLE_ID.sub("PrintF"), 1)
			.addPtr("text", VAL_TYPE)
			.returnVoid(c -> new PrintFn(c));

	private PrintFn(FuncCaller<PrintFn> caller) {
		super(caller);
	}

	public void print(Code code, ValOp text) {
		invoke(null, code, PRINT.result(), text.ptr(code));
	}

}
