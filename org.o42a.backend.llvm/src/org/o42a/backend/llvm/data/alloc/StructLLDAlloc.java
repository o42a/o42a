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
package org.o42a.backend.llvm.data.alloc;

import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.backend.llvm.id.TempLLVMId;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public final class StructLLDAlloc<S extends StructOp<S>>
		extends ContainerLLDAlloc<S> {

	private final LLVMId llvmId;
	private final TempLLVMId tempId;

	public StructLLDAlloc(
			long typePtr,
			long nativePtr,
			ContainerLLDAlloc<?> enclosing,
			Type<S> type,
			String displayName) {
		super(enclosing.getModule(), typePtr, nativePtr, enclosing, type);
		if (isTypeAllocated()) {
			this.llvmId = enclosing.nextId();
			this.tempId = null;
		} else {
			this.llvmId = this.tempId = enclosing.llvmId().addTemp(displayName);
		}
	}

	@Override
	public LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public void setNativePtr(long nativePtr) {
		super.setNativePtr(nativePtr);
		if (this.tempId != null) {
			getEnclosing().layout(this);
			this.tempId.refine(getEnclosing().nextId());
		}
	}

}
