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

import org.o42a.backend.llvm.code.op.LLVMAnyOp;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.backend.DataAllocation;


public final class AnyDataAlloc extends SimpleDataAllocation<AnyOp> {

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
	public DataAllocation<AnyOp> toAny() {
		return this;
	}

	@Override
	protected LLVMAnyOp op(long blockPtr, long nativePtr) {
		return new LLVMAnyOp(blockPtr, nativePtr);
	}

}
