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
package org.o42a.backend.llvm.data;

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.backend.llvm.code.LLVMStruct;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.Type;


abstract class SimpleDataAllocation<O extends PtrOp>
		extends LLVMDataAllocation<O> {

	private final LLVMId llvmId;

	SimpleDataAllocation(ContainerAllocation<?> enclosing) {
		super(enclosing.getModule(), enclosing);
		this.llvmId = enclosing.nextId();
	}

	SimpleDataAllocation(
			LLVMModule module,
			LLVMId llvmId,
			ContainerAllocation<?> enclosing) {
		super(module, enclosing);
		this.llvmId = llvmId;
	}

	@Override
	public final LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public O op(CodeWriter writer) {

		final LLVMCode code = (LLVMCode) writer;

		return op(code.nextPtr(), llvmId().expression(code.getModule()));
	}

	protected abstract O op(long blockPtr, long nativePtr);

	static final class StructPtr<O extends StructOp>
			extends SimpleDataAllocation<O> {

		private final Type<O> type;

		public StructPtr(
				ContainerAllocation<?> enclosing,
				Type<O> type) {
			super(enclosing);
			this.type = type;
		}

		@Override
		public DataLayout getLayout() {
			return getModule().dataAllocator().ptrLayout();
		}

		@Override
		protected O op(long blockPtr, long nativePtr) {
			return this.type.op(
					new LLVMStruct(this.type, blockPtr, nativePtr));
		}

	}

}
