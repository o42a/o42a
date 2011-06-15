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
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public class LLVMFunc<F extends Func<F>> extends LLVMPtrOp
		implements FuncCaller<F> {

	private final Signature<F> signature;

	public LLVMFunc(
			CodeId id,
			Signature<F> signature,
			long blockPtr,
			long nativePtr) {
		super(id, CONSTANT_ALLOC_CLASS, 0L, nativePtr);
		this.signature = signature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public LLVMFunc<F> create(CodeId id, long blockPtr, long nativePtr) {
		return new LLVMFunc<F>(id, this.signature, blockPtr, getNativePtr());
	}

	@Override
	public void call(Code code, Op... args) {
		call(nextPtr(code), null, args);
	}

	@Override
	public Int8op callInt8(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt8op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public Int16op callInt16(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt16op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public Int32op callInt32(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt32op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public Int64op callInt64(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt64op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public Fp32op callFp32(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp32op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public Fp64op callFp64(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp64op(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public BoolOp callBool(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(id, nextPtr, call(nextPtr, id, args));
	}

	@Override
	public AnyOp callAny(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMAnyOp(
				id,
				getAllocClass(),
				nextPtr,
				call(nextPtr, id, args));
	}

	@Override
	public DataOp callData(CodeId id, Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMDataOp(
				id,
				getAllocClass(),
				nextPtr,
				call(nextPtr, id, args));
	}

	@Override
	public <O extends StructOp> O callPtr(
			CodeId id,
			Code code,
			Type<O> type,
			Op... args) {

		final long nextPtr = nextPtr(code);

		return type.op(new LLVMStruct(
				id,
				AUTO_ALLOC_CLASS,
				type,
				nextPtr,
				call(nextPtr, id, args)));
	}

	private long call(long blockPtr, CodeId id, Op[] args) {

		final long[] argPtrs = new long[args.length];

		for (int i = 0; i < args.length; ++i) {
			argPtrs[i] = nativePtr(args[i]);
		}

		return call(
				blockPtr,
				id != null ? id.toString() : null,
				getNativePtr(),
				argPtrs);
	}

	private static native long call(
			long blockPtr,
			String id,
			long functionPtr,
			long[] argPtrs);

}
