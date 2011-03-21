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

import static org.o42a.backend.llvm.code.LLVMCode.*;

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public abstract class LLVMPtrOp implements LLVMOp, PtrOp {

	private final long blockPtr;
	private final long nativePtr;

	public LLVMPtrOp(long blockPtr, long nativePtr) {
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public final long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
	}

	@Override
	public void returnValue(Code code) {
		llvm(code).returnValue(this);
	}

	@Override
	public LLVMBoolOp isNull(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(nextPtr, isNull(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMBoolOp eq(Code code, PtrOp other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				LLVMIntOp.eq(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMAnyOp toAny(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMAnyOp(nextPtr, toAny(nextPtr, getNativePtr()));
	}

	public LLVMDataOp toData(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp(nextPtr, toAny(nextPtr, getNativePtr()));
	}

	public <O extends StructOp> O to(Code code, Type<O> type) {
		final long nextPtr = nextPtr(code);

		return type.op(new LLVMStruct(
				type,
				nextPtr,
				castStructTo(nextPtr, getNativePtr(), typePtr(type))));
	}

	public <F extends Func> LLVMFuncOp<F> toFunc(
			Code code,
			Signature<F> signature) {

		final LLVMCode llvm = LLVMCode.llvm(code);
		final long nextPtr = llvm.nextPtr();

		return new LLVMFuncOp<F>(
				nextPtr,
				castFuncTo(
						nextPtr,
						getNativePtr(),
						llvm.getModule().nativePtr(signature)),
				signature);
	}

	protected static native long field(
			long blockPtr,
			long pointerPtr,
			int field);

	static native long load(long blockPtr, long pointerPtr);

	static native void store(
			long blockPtr,
			long pointerPtr,
			long valuePtr);

	static native long toAny(long blockPtr, long pointerPtr);

	static native long toPtr(long blockPtr, long pointerPtr);

	static native long toInt(long blockPtr, long pointerPtr, byte numBits);

	static native long toFp32(long blockPtr, long pointerPtr);

	static native long toFp64(long blockPtr, long pointerPtr);

	static native long toRelPtr(long blockPtr, long pointerPtr);

	private static native long castStructTo(
			long blockPtr,
			long pointerPtr,
			long typePtr);

	private static native long castFuncTo(
			long blockPtr,
			long pointerPtr,
			long funcTypePtr);

	private static native long isNull(long blockPtr, long pointerPtr);

}
