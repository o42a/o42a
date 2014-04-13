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

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.rec.AtomicRecLLOp;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public final class FuncLLOp<F extends Func<F>>
		extends AtomicRecLLOp<FuncOp<F>, F>
		implements FuncOp<F> {

	private final Signature<F> signature;

	public FuncLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr,
			Signature<F> signature) {
		super(id, allocPlace, blockPtr, nativePtr);
		this.signature = signature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public <FF extends Func<FF>> FuncLLOp<FF> toFunc(
			ID id,
			Code code,
			Signature<FF> signature) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();

		return new FuncLLOp<>(
				id,
				getAllocPlace(),
				nextPtr,
				llvm.instr(castFuncTo(
						nextPtr,
						llvm.nextInstr(),
						ids.write(id),
						ids.length(),
						getNativePtr(),
						llvm.getModule().nativePtr(signature))),
				signature);
	}

	@Override
	protected F createLoaded(ID id, long blockPtr, long nativePtr) {
		return getSignature().op(new LLFunc<>(
				id,
				getSignature(),
				blockPtr,
				nativePtr));
	}

	@Override
	public FuncLLOp<F> create(ID id, long blockPtr, long nativePtr) {
		return new FuncLLOp<>(id, null, blockPtr, nativePtr, getSignature());
	}

	@Override
	public String toString() {
		return "(" + this.signature.getId() + "*) " + getId();
	}

}
