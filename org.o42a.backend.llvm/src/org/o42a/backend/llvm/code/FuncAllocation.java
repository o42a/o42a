/*
    Compiler LLVM Back-end
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.backend.llvm.data.*;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.CodeAllocation;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;


public final class FuncAllocation<F extends Func>
		implements CodeAllocation<F>, LLVMAllocation {

	private final LLVMModule module;
	private final LLVMId llvmId;
	private final Signature<F> signature;

	public FuncAllocation(
			LLVMModule module,
			LLVMId llvmId,
			Signature<F> signature) {
		this.module = module;
		this.llvmId = llvmId;
		this.signature = signature;
	}

	public FuncAllocation(
			LLVMModule module,
			long nativePtr,
			Signature<F> signature) {
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

	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return new AnyDataAlloc(getModule(), llvmId().toAny(), null);
	}

	@Override
	public void write(DataWriter writer) {
		llvmId().write(writer);
	}

	@Override
	public String toString() {
		return this.llvmId.toString();
	}

}
