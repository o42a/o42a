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

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.NumOp;
import org.o42a.codegen.code.op.StructOp;


public abstract class LLVMNumOp<O extends NumOp<O>, T extends O>
		implements LLVMOp, NumOp<O> {

	private final CodeId id;
	private final long blockPtr;
	private final long nativePtr;

	public LLVMNumOp(CodeId id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final CodeId getId() {
		return this.id;
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
	public void allocated(Code code, StructOp<?> enclosing) {
	}

	@Override
	public abstract T neg(CodeId id, Code code);

	@Override
	public abstract T add(CodeId id, Code code, O summand);

	@Override
	public abstract T sub(CodeId id, Code code, O subtrahend);

	@Override
	public abstract T mul(CodeId id, Code code, O multiplier);

	@Override
	public abstract T div(CodeId id, Code code, O divisor);

	@Override
	public abstract T rem(CodeId id, Code code, O divisor);

	@Override
	public abstract LLVMInt8op toInt8(CodeId id, Code code);

	@Override
	public abstract LLVMInt16op toInt16(CodeId id, Code code);

	@Override
	public abstract LLVMInt32op toInt32(CodeId id, Code code);

	@Override
	public abstract LLVMInt64op toInt64(CodeId id, Code code);

	@Override
	public abstract LLVMFp32op toFp32(CodeId id, Code code);

	@Override
	public abstract LLVMFp64op toFp64(CodeId id, Code code);

	@Override
	public abstract T create(CodeId id, long blockPtr, long nativePtr);

	@Override
	public void returnValue(Code code) {
		llvm(code).returnValue(this);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected final CodeId castId(CodeId id, Code code, String suffix) {
		return LLVMCode.castId(this, id, code, suffix);
	}

	protected final CodeId castId(CodeId id, Code code, CodeId suffix) {
		return LLVMCode.castId(this, id, code, suffix);
	}

	protected final CodeId unaryId(CodeId id, Code code, String op) {
		return LLVMCode.unaryId(this, id, code, op);
	}

	protected final CodeId binaryId(
			CodeId id,
			Code code,
			String op,
			O operand) {
		return LLVMCode.binaryId(this, id, code, op, operand);
	}

}
