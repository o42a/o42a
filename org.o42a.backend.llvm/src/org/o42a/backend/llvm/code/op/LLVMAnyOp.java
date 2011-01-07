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
import org.o42a.codegen.code.op.AnyOp;


public final class LLVMAnyOp extends LLVMPtrOp implements AnyOp {

	public LLVMAnyOp(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public LLVMAnyOp toAny(Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toAny(code);
	}

	@Override
	public LLVMAnyOp create(long blockPtr, long nativePtr) {
		return new LLVMAnyOp(blockPtr, nativePtr);
	}

}
