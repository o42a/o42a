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
import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;


public final class LLVMInt64op extends LLVMIntOp<Int64op, LLVMInt64op>
		implements Int64op {

	public LLVMInt64op(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public LLVMInt64op toInt64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toInt64(id, code);
	}

	@Override
	public LLVMInt64op create(CodeId id, long blockPtr, long nativePtr) {
		return new LLVMInt64op(id, blockPtr, nativePtr);
	}

	@Override
	protected LLVMInt64op constantValue(Code code, int value) {
		return llvm(code).int64(value);
	}

}
