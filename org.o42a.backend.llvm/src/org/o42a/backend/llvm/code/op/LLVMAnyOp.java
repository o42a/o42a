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
import org.o42a.codegen.code.op.*;


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
	public LLVMRecOp<AnyOp> toPtr(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(nextPtr, toPtr(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<Int32op> toInt32(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int32(nextPtr, toInt32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<Int64op> toInt64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int64(nextPtr, toInt64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<Fp64op> toFp64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp64(nextPtr, toFp64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<RelOp> toRel(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Rel(nextPtr, toRelPtr(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMAnyOp create(long blockPtr, long nativePtr) {
		return new LLVMAnyOp(blockPtr, nativePtr);
	}

}
