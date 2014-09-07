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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;


public class PrintMessageFunc extends Func<PrintMessageFunc> {

	public static final ExtSignature<Void, PrintMessageFunc> PRINT_MESSAGE =
			customSignature("PrintMessageF", 1)
			.addPtr("message")
			.returnVoid(c -> new PrintMessageFunc(c));

	private PrintMessageFunc(FuncCaller<PrintMessageFunc> caller) {
		super(caller);
	}

	public void print(Code code, AnyOp message) {
		invoke(null, code, PRINT_MESSAGE.result(), message);
	}

}
