/*
    Compiler LLVM Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int8op;
import org.o42a.util.string.ID;


public final class Int8llOp extends IntLLOp<Int8op, Int8llOp>
		implements Int8op {

	public Int8llOp(ID id, long blockPtr, long nativePtr) {
		super(id, 8, blockPtr, nativePtr);
	}

	@Override
	public Int8op comp(ID id, Code code) {
		return xor(
				code.getOpNames().unaryId(id, COMP_ID, this),
				code,
				code.int8((byte) -1));
	}

	@Override
	public Int8llOp toInt8(ID id, Code code) {

		final long nextPtr = llvm(code).nextPtr();

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toInt8(id, code);
	}

	@Override
	public Int8llOp create(ID id, long blockPtr, long nativePtr) {
		return new Int8llOp(id, blockPtr, nativePtr);
	}

	@Override
	protected Int8llOp constantValue(Code code, int value) {
		return llvm(code).int8((byte) value);
	}

}
