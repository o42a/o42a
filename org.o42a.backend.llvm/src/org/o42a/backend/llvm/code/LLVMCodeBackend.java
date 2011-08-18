/*
    Compiler LLVM Back-end
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.backend.llvm.data.LLVMId.codeId;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.*;


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
		return new LLVMSignatureWriter<F>(this.module, signature);
	}

	@Override
	public <F extends Func<F>> LLVMFunction<F> addFunction(
			Function<F> function,
			CodeCallback callback) {
		return new LLVMFunction<F>(this.module, function, callback);
	}

	@Override
	public <F extends Func<F>> FuncAllocation<F> externFunction(
			CodeId id,
			Signature<F> signature) {

		final long functionPtr = LLVMFunction.externFunction(
				this.module.getNativePtr(),
				id.getId(),
				getModule().nativePtr(signature));

		return new LLVMFuncAllocation<F>(
				this.module,
				codeId(id, functionPtr),
				signature);
	}

}
