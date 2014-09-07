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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.op.CodeDirs;


public final class ValCopyFn extends Fn<ValCopyFn> {

	public static final ExtSignature<Void, ValCopyFn> VAL_COPY =
			customSignature("ValCopyF", 2)
			.addPtr("from", VAL_TYPE)
			.addPtr("to", VAL_TYPE)
			.returnVoid(c -> new ValCopyFn(c));

	private ValCopyFn(FuncCaller<ValCopyFn> caller) {
		super(caller);
	}

	public void copy(CodeDirs dirs, ValOp from, ValOp to) {

		final Block code = dirs.code();

		invoke(null, code, VAL_COPY.result(), from.ptr(code), to.ptr(code));
		to.go(code, dirs);
		to.holder().hold(code);
	}

}
