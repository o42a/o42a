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
package org.o42a.backend.llvm.code.rec;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.op.AllocPtrLLOp;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Atomicity;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public abstract class RecLLOp<R extends RecOp<R, O>, O extends Op>
		extends AllocPtrLLOp<R>
		implements RecOp<R, O> {

	public RecLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		super(id, allocPlace, blockPtr, nativePtr);
	}

	@Override
	public final O load(ID id, Code code) {
		return load(id, code, NOT_ATOMIC);
	}

	public final O load(ID id, Code code, Atomicity atomicity) {
		assert getAllocPlace().ensureAccessibleFrom(code);

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().derefId(id, this);

		return createLoaded(
				resultId,
				nextPtr,
				llvm.instr(load(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						atomicity.code())));
	}

	@Override
	public final void store(Code code, O value) {
		store(code, value, NOT_ATOMIC);
	}

	public final void store(Code code, O value, Atomicity atomicity) {
		assert getAllocPlace().ensureAccessibleFrom(code);

		final LLCode llvm = llvm(code);

		llvm.instr(store(
				llvm.nextPtr(),
				llvm.nextInstr(),
				getNativePtr(),
				nativePtr(value),
				atomicity.code()));
	}

	protected abstract O createLoaded(ID id, long blockPtr, long nativePtr);

}
