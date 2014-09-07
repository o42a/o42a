/*
    Compiler Core
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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.FuncCaller;


public final class ValUseFunc extends Func<ValUseFunc> {

	public static final ExtSignature<Void, ValUseFunc> VAL_USE =
			customSignature("ValUseF", 1)
			.addPtr("val", VAL_TYPE)
			.returnVoid(c -> new ValUseFunc(c));

	private ValUseFunc(FuncCaller<ValUseFunc> caller) {
		super(caller);
	}

	public final void call(Code code, ValType.Op val) {
		invoke(null, code, VAL_USE.result(), val);
	}

}
