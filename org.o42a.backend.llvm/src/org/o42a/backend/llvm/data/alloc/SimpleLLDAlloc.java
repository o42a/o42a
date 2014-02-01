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

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.string.ID;


public abstract class SimpleLLDAlloc<P extends PtrOp<P>> extends LLDAlloc<P> {

	private LLVMId llvmId;

	public SimpleLLDAlloc(
			ContainerLLDAlloc<?> enclosing,
			DataAllocation<P> proto) {
		super(enclosing.getModule(), enclosing);
		if (proto != null) {

			final SimpleLLDAlloc<P> protoAlloc = (SimpleLLDAlloc<P>) proto;

			this.llvmId =
					enclosing.llvmId().addIndex(protoAlloc.llvmId().getIndex());
		}
	}

	public SimpleLLDAlloc(
			LLVMModule module,
			LLVMId llvmId,
			ContainerLLDAlloc<?> enclosing) {
		super(module, enclosing);
		this.llvmId = llvmId;
	}

	@Override
	public final LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public P op(ID id, AllocClass allocClass, CodeWriter writer) {

		final LLCode code = (LLCode) writer;

		return op(
				id,
				allocClass.allocPlace(code.code()),
				code.nextPtr(),
				llvmId().expression(code.getModule()));
	}

	protected abstract P op(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr);

	protected void init() {
		if (this.llvmId == null) {

			final ContainerLLDAlloc<?> enclosing = getEnclosing();

			enclosing.layout(getLayout());
			this.llvmId = enclosing.nextId();
		}
	}

}
