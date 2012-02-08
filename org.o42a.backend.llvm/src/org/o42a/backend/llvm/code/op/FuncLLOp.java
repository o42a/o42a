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
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.AllocClass;


public final class FuncLLOp<F extends Func<F>>
		extends PtrLLOp<FuncOp<F>>
		implements FuncOp<F> {

	private final Signature<F> signature;

	public FuncLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr,
			Signature<F> signature) {
		super(id, allocClass, blockPtr, nativePtr);
		this.signature = signature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final F load(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = derefId(id, code);

		return getSignature().op(new LLFunc<F>(
				resultId,
				getSignature(),
				nextPtr,
				llvm.instr(load(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()))));
	}

	@Override
	public final void store(Code code, F value) {

		final LLCode llvm = llvm(code);

		llvm.instr(store(
				llvm.nextPtr(),
				llvm.nextInstr(),
				getNativePtr(),
				nativePtr(value)));
	}

	@Override
	public FuncLLOp<F> create(CodeId id, long blockPtr, long nativePtr) {
		return new FuncLLOp<F>(id, null, blockPtr, nativePtr, getSignature());
	}

	@Override
	public String toString() {
		return "(" + this.signature.getId() + "*) " + getId();
	}

}
