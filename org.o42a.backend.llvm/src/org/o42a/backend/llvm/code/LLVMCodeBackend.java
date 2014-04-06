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
package org.o42a.backend.llvm.code;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.id.LLVMId.dataId;
import static org.o42a.backend.llvm.id.LLVMId.extenFuncId;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.AnyLLDAlloc;
import org.o42a.backend.llvm.data.alloc.LLFAlloc;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


public class LLVMCodeBackend implements CodeBackend {

	private final LLVMModule module;

	public LLVMCodeBackend(LLVMModule module) {
		this.module = module;
	}

	public final LLVMModule getModule() {
		return this.module;
	}

	@Override
	public <F extends Func<F>> SignatureWriter<F> addSignature(
			Signature<F> signature) {
		return new LLSignatureWriter<>(this.module, signature);
	}

	@Override
	public <F extends Func<F>> LLFunction<F> addFunction(
			Function<F> function,
			Disposal beforeReturn) {
		return new LLFunction<>(this.module, function, beforeReturn);
	}

	@Override
	public <F extends Func<F>> FuncAllocation<F> externFunction(
			ID id,
			FuncPtr<F> pointer) {

		final Signature<F> signature = pointer.getSignature();
		final NativeBuffer ids = this.module.ids();
		final long functionPtr = LLFunction.externFunction(
				this.module.getNativePtr(),
				ids.write(id),
				ids.length(),
				getModule().nativePtr(signature));

		return new LLFAlloc<>(
				this.module,
				extenFuncId(id, functionPtr),
				signature);
	}

	@Override
	public DataAllocation<AnyOp> codeToAny(CodePtr ptr) {

		final LLCodePos llvmPos = llvm(ptr.pos());

		return new AnyLLDAlloc(
				getModule(),
				dataId(ptr.getId(), codeToAny(llvmPos.getBlockPtr())),
				null);
	}

	private static native long codeToAny(long blockPtr);

}
