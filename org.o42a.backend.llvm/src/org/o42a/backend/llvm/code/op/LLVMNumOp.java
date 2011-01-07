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

import static org.o42a.backend.llvm.code.LLVMCode.llvm;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.NumOp;


public abstract class LLVMNumOp<O extends NumOp<O>, T extends O>
		implements LLVMOp, NumOp<O> {

	private final long blockPtr;
	private final long nativePtr;

	public LLVMNumOp(long blockPtr, long nativePtr) {
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
	public abstract T neg(Code code);

	@Override
	public abstract T add(Code code, O summand);

	@Override
	public abstract T sub(Code code, O subtrahend);

	@Override
	public abstract T mul(Code code, O multiplier);

	@Override
	public abstract T div(Code code, O divisor);

	@Override
	public abstract T rem(Code code, O divisor);

	@Override
	public abstract LLVMInt32op toInt32(Code code);

	@Override
	public abstract LLVMInt64op toInt64(Code code);

	@Override
	public abstract LLVMFp64op toFp64(Code code);

	@Override
	public abstract T create(long blockPtr, long nativePtr);

	@Override
	public void returnValue(Code code) {
		llvm(code).returnValue(this);
	}

}
