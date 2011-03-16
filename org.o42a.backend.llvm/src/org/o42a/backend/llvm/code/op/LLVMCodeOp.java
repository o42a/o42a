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
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;


public final class LLVMCodeOp<F extends Func>
		extends LLVMPtrOp
		implements FuncOp<F> {

	private final Signature<F> signature;

	public LLVMCodeOp(long blockPtr, long nativePtr, Signature<F> signature) {
		super(blockPtr, nativePtr);
		this.signature = signature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final F load(Code code) {

		final long nextPtr = nextPtr(code);

		return getSignature().op(new LLVMFunc<F>(
				nextPtr,
				load(nextPtr, getNativePtr())));
	}

	@Override
	public final void store(Code code, F value) {
		store(nextPtr(code), getNativePtr(), nativePtr(value));
	}

	@Override
	public LLVMCodeOp<F> create(long blockPtr, long nativePtr) {
		return new LLVMCodeOp<F>(blockPtr, nativePtr, this.signature);
	}

}
