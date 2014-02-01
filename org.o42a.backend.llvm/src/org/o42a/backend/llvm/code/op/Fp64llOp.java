/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.llvm;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.util.string.ID;


public final class Fp64llOp extends FpLLOp<Fp64op, Fp64llOp>
		implements Fp64op {

	public Fp64llOp(ID id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public Fp64llOp toFp64(ID id, Code code) {

		final long nextPtr = llvm(code).nextPtr();

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toFp64(id, code);
	}

	@Override
	public Fp64llOp create(ID id, long blockPtr, long nativePtr) {
		return new Fp64llOp(id, blockPtr, nativePtr);
	}

}
