/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nextPtr;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int32op;


public final class Int32llOp extends IntLLOp<Int32op, Int32llOp>
		implements Int32op {

	public Int32llOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, 32, blockPtr, nativePtr);
	}

	@Override
	public Int32op comp(CodeId id, Code code) {
		return xor(unaryId(id, code, "comp"), code, code.int32(-1));
	}

	@Override
	public Int32llOp toInt32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toInt32(id, code);
	}

	@Override
	public Int32llOp create(CodeId id, long blockPtr, long nativePtr) {
		return new Int32llOp(id, blockPtr, nativePtr);
	}

	@Override
	protected Int32llOp constantValue(Code code, int value) {
		return llvm(code).int32(value);
	}

}
