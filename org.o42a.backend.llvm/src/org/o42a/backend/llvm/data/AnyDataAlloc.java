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

import org.o42a.backend.llvm.code.op.LLVMDataOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.backend.DataAllocation;


public final class AnyDataAlloc extends SimpleDataAllocation<DataOp> {

	AnyDataAlloc(ContainerAllocation<?> enclosing) {
		super(enclosing);
	}

	public AnyDataAlloc(
			LLVMModule module,
			LLVMId id,
			ContainerAllocation<?> enclosing) {
		super(module, id, enclosing);
	}

	@Override
	public DataLayout getLayout() {
		return getModule().dataAllocator().ptrLayout();
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return this;
	}

	@Override
	protected LLVMDataOp op(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		return new LLVMDataOp(id, allocClass, blockPtr, nativePtr);
	}

}
