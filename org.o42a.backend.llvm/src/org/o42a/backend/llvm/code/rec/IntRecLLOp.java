/*
    Compiler LLVM Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.llvm.code.rec;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.IntRecOp;
import org.o42a.codegen.code.op.RMWKind;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public abstract class IntRecLLOp<R extends IntRecOp<R, O>, O extends IntOp<O>>
		extends AtomicRecLLOp<R, O>
		implements IntRecOp<R, O> {

	public IntRecLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		super(id, allocPlace, blockPtr, nativePtr);
	}

	@Override
	public O atomicRMW(ID id, Code code, RMWKind kind, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(
				id,
				kind.getId(),
				this,
				operand);

		return createLoaded(
				resultId,
				nextPtr,
				llvm.instr(nextPtr, atomicRMW(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						kind.code(),
						nativePtr(operand))));
	}

}
