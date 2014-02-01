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

import org.o42a.backend.llvm.code.rec.StructRecLLOp;
import org.o42a.backend.llvm.data.alloc.ContainerLLDAlloc;
import org.o42a.backend.llvm.data.alloc.SimpleLLDAlloc;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public final class StructRecLLDAlloc<S extends StructOp<S>>
		extends SimpleLLDAlloc<StructRecOp<S>> {

	private final Type<S> type;

	public StructRecLLDAlloc(
			ContainerLLDAlloc<?> enclosing,
			DataAllocation<StructRecOp<S>> proto,
			Type<S> type) {
		super(enclosing, proto);
		this.type = type;
		init();
	}

	@Override
	public DataLayout getLayout() {
		return getModule().dataAllocator().ptrLayout();
	}

	@Override
	protected StructRecOp<S> op(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		return new StructRecLLOp<>(
				id,
				allocPlace,
				this.type,
				blockPtr,
				nativePtr);
	}

}
