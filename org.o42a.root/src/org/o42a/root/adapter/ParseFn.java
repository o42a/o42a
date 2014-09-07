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
package org.o42a.root.adapter;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public final class ParseFn extends Fn<ParseFn> {

	public static final ExtSignature<Void, ParseFn> PARSE =
			customSignature("ParseF", 2)
			.addPtr("output", VAL_TYPE)
			.addPtr("input", VAL_TYPE)
			.returnVoid(c -> new ParseFn(c));

	private ParseFn(FuncCaller<ParseFn> caller) {
		super(caller);
	}

	public ValOp parse(ValDirs dirs, ValOp input) {

		final Block code = dirs.code();
		final ValOp output = dirs.value();

		parse(code, output, input);
		output.go(code, dirs);

		return output;
	}

	public void parse(Code code, ValOp output, ValOp input) {
		invoke(null, code, PARSE.result(), output.ptr(code), input.ptr(code));
	}

}
