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
import org.o42a.codegen.code.op.*;
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

	@Override
	public LLVMDataOp<AnyOp> toPtr(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Any(nextPtr, toPtr(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMDataOp<Int32op> toInt32(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Int32(nextPtr, toInt32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMDataOp<Int64op> toInt64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Int64(nextPtr, toInt64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMDataOp<Fp64op> toFp64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Fp64(nextPtr, toFp64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMDataOp<RelOp> toRel(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp.Rel(nextPtr, toRelPtr(nextPtr, getNativePtr()));
	}

	@Override
	public <F extends Func> LLVMCodeOp<F> toFunc(
			Code code,
			Signature<F> signature) {

		final LLVMCode llvm = LLVMCode.llvm(code);
		final long nextPtr = llvm.nextPtr();

		return new LLVMCodeOp<F>(
				nextPtr,
				castFuncTo(nextPtr, getNativePtr(), nativePtr(signature)),
				signature);
	}

	@Override
	public <O extends StructOp> O to(Code code, Type<O> type) {

		final long nextPtr = nextPtr(code);

		return type.op(new LLVMStruct(
				type,
				nextPtr,
				castStructTo(nextPtr, getNativePtr(), typePtr(type))));
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

	private static native long toAny(long blockPtr, long pointerPtr);

	private static native long toPtr(long blockPtr, long pointerPtr);

	private static native long toInt32(long blockPtr, long pointerPtr);

	private static native long toInt64(long blockPtr, long pointerPtr);

	private static native long toFp64(long blockPtr, long pointerPtr);

	private static native long toRelPtr(long blockPtr, long pointerPtr);

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
