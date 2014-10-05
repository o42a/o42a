/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.util.string.ID;


public final class RelLLOp extends LLOp<RelOp> implements RelOp {

	public RelLLOp(ID id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public AnyLLOp offset(ID id, Code code, DataPtrOp<?> from) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.opNames().offsetId(id, from, this);

		return new AnyLLOp(
				resultId,
				from.getAllocPlace(),// Points to the same allocation unit.
				nextPtr,
				llvm.instr(offsetBy(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						nativePtr(from),
						getNativePtr())));
	}

	@Override
	public Int32llOp toInt32(ID id, Code code) {
		return new Int32llOp(
				code.opNames().castId(id, INT32_ID, this),
				getBlockPtr(),
				getNativePtr());
	}

	@Override
	public RelLLOp create(ID id, long blockPtr, long nativePtr) {
		return new RelLLOp(id, blockPtr, nativePtr);
	}

	private static native long offsetBy(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long fromPtr,
			long byPtr);

}
