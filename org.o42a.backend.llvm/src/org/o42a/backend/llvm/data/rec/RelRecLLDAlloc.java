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
package org.o42a.backend.llvm.data.rec;

import org.o42a.backend.llvm.code.rec.RelRecLLOp;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.SimpleLLDAlloc;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public final class RelRecLLDAlloc extends SimpleLLDAlloc<RelRecOp> {

	public RelRecLLDAlloc(
			ContainerLLDAlloc<?> enclosing,
			DataAllocation<RelRecOp> proto) {
		super(enclosing, proto);
		init();
	}

	@Override
	public DataLayout getLayout() {
		return getModule().dataAllocator().relPtrLayout();
	}

	@Override
	protected RelRecLLOp op(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		return new RelRecLLOp(id, allocPlace, blockPtr, nativePtr);
	}

}
