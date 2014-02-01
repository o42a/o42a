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

import static org.o42a.backend.llvm.id.LLVMId.nullId;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public final class NullStructLLDAlloc<S extends StructOp<S>>
		extends ContainerLLDAlloc<S> {

	private final LLVMId llvmId;

	public NullStructLLDAlloc(LLVMModule module, long nativePtr, Type<S> type) {
		super(
				module,
				((ContainerLLDAlloc<?>)
						type.pointer(module.getGenerator())
						.getAllocation()).getTypePtr(),
				nativePtr,
				null,
				type);
		this.llvmId = nullId(nativePtr, false);
	}

	@Override
	public LLVMId llvmId() {
		return this.llvmId;
	}

}
