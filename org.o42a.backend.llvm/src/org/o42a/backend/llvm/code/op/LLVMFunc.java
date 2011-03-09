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

import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public class LLVMFunc<F extends Func> extends LLVMPtrOp implements FuncCaller {

	public LLVMFunc(long blockPtr, long nativePtr) {
		super(0L, nativePtr);
	}

	@Override
	public LLVMFunc<F> create(long blockPtr, long nativePtr) {
		return new LLVMFunc<F>(blockPtr, getNativePtr());
	}

	@Override
	public void call(Code code, Op... args) {
		call(nextPtr(code), args);
	}

	@Override
	public Int32op callInt32(Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt32op(nextPtr, call(nextPtr, args));
	}

	@Override
	public Int64op callInt64(Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt64op(nextPtr, call(nextPtr, args));
	}

	@Override
	public Fp64op callFp64(Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp64op(nextPtr, call(nextPtr, args));
	}

	@Override
	public BoolOp callBool(Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(nextPtr, call(nextPtr, args));
	}

	@Override
	public AnyOp callAny(Code code, Op... args) {

		final long nextPtr = nextPtr(code);

		return new LLVMAnyOp(nextPtr, call(nextPtr, args));
	}

	@Override
	public <O extends StructOp> O callPtr(Code code, Type<O> type, Op... args) {

		final long nextPtr = nextPtr(code);

		return type.op(new LLVMStruct(type, nextPtr, call(nextPtr, args)));
	}

	private long call(long blockPtr, Op[] args) {

		final long[] argPtrs = new long[args.length];

		for (int i = 0; i < args.length; ++i) {
			argPtrs[i] = nativePtr(args[i]);
		}

		return call(blockPtr, getNativePtr(), argPtrs);
	}

	private static native long call(
			long blockPtr,
			long functionPtr,
			long[] argPtrs);

}
