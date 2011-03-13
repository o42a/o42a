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

import static org.o42a.backend.llvm.code.LLVMCode.nativePtr;
import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.StructOp;


public final class LLVMRelOp implements LLVMOp, RelOp {

	private final long blockPtr;
	private final long nativePtr;

	public LLVMRelOp(long blockPtr, long nativePtr) {
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
	}

	@Override
	public LLVMAnyOp offset(Code code, PtrOp from) {

		final long nextPtr = nextPtr(code);

		return new LLVMAnyOp(
				nextPtr,
				offsetBy(nextPtr, nativePtr(from), getNativePtr()));
	}

	@Override
	public LLVMInt32op toInt32(Code code) {
		return new LLVMInt32op(getBlockPtr(), getNativePtr());
	}

	@Override
	public LLVMRelOp create(long blockPtr, long nativePtr) {
		return new LLVMRelOp(blockPtr, nativePtr);
	}

	private static native long offsetBy(
			long blockPtr,
			long fromPtr,
			long byPtr);

}
