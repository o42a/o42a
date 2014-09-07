/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;


public final class VariableUseFn extends Fn<VariableUseFn> {

	public static final ExtSignature<DataOp, VariableUseFn> VARIABLE_USE =
			customSignature("VariableUseF", 1)
			.addPtr("var")
			.returnData(c -> new VariableUseFn(c));

	private VariableUseFn(FuncCaller<VariableUseFn> caller) {
		super(caller);
	}

	public DataOp use(Code code, DataRecOp var) {
		return invoke(null, code, VARIABLE_USE.result(), var.toAny(null, code));
	}

}
