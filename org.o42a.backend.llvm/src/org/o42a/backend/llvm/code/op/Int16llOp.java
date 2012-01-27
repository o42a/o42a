/*
    Compiler LLVM Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.codegen.code.op.Int16op;


public final class Int16llOp extends IntLLOp<Int16op, Int16llOp>
		implements Int16op {

	public Int16llOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, 16, blockPtr, nativePtr);
	}

	@Override
	public Int16op comp(CodeId id, Code code) {
		return xor(unaryId(id, code, "comp"), code, code.int16((short) -1));
	}

	@Override
	public Int16llOp toInt16(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toInt16(id, code);
	}

	@Override
	public Int16llOp create(CodeId id, long blockPtr, long nativePtr) {
		return new Int16llOp(id, blockPtr, nativePtr);
	}

	@Override
	protected Int16llOp constantValue(Code code, int value) {
		return llvm(code).int16((short) value);
	}

}
