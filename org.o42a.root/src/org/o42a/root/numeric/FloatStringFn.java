/*
    Root Object Definition
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
package org.o42a.root.numeric;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public class FloatStringFn extends Fn<FloatStringFn> {

	public static final ExtSignature<BoolOp, FloatStringFn> FLOAT_STRING =
			customSignature("FloatStringF", 2)
			.addPtr("string", ValType.VAL_TYPE)
			.addFp64("value")
			.returnBool(c -> new FloatStringFn(c));

	private FloatStringFn(FuncCaller<FloatStringFn> caller) {
		super(caller);
	}

	public ValOp convert(ValDirs stringDirs, Fp64op value) {

		final Block code = stringDirs.code();
		final ValOp string = stringDirs.value();
		final BoolOp result = invoke(
				null,
				code,
				FLOAT_STRING.result(),
				string.ptr(code),
				value);

		result.goUnless(code, stringDirs.falseDir());

		return string;
	}

}
