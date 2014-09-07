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
package org.o42a.backend.llvm.data.alloc;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.FuncAllocation;


public final class LLFAlloc<F extends Fn<F>>
		implements FuncAllocation<F>, LLAlloc {

	private final LLVMModule module;
	private final LLVMId llvmId;
	private final Signature<F> signature;

	public LLFAlloc(LLVMModule module, LLVMId llvmId, Signature<F> signature) {
		this.module = module;
		this.llvmId = llvmId;
		this.signature = signature;
	}

	public LLFAlloc(LLVMModule module, long nativePtr, Signature<F> signature) {
		this.module = module;
		this.llvmId = LLVMId.nullId(nativePtr, true);
		this.signature = signature;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	@Override
	public final LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return new AnyLLDAlloc(getModule(), llvmId().toAny(), null);
	}

	@Override
	public void write(
			DataWriter writer,
			DataAllocation<FuncOp<F>> destination) {
		llvmId().write(writer, destination);
	}

	@Override
	public String toString() {
		return this.llvmId.toString();
	}

}
