/*
    Compiler LLVM Back-end
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Fp64op;


public final class LLVMFp64op extends LLVMFpOp<Fp64op, LLVMFp64op>
		implements Fp64op {

	public LLVMFp64op(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public LLVMFp64op toFp64(Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toFp64(code);
	}

	@Override
	public LLVMFp64op create(long blockPtr, long nativePtr) {
		return new LLVMFp64op(blockPtr, nativePtr);
	}

}
