/*
    Compiler LLVM Back-end
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.backend.llvm.data.LLVMId.typeId;

import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.Type;


public final class TypeAllocation<O extends PtrOp>
		extends ContainerAllocation<O> {

	private final LLVMId llvmId;

	TypeAllocation(
			LLVMModule module,
			long nativePtr,
			ContainerAllocation<?> prev,
			Type<O> type) {
		super(
				module,
				0L,
				nativePtr,
				prev,
				type);
		this.llvmId = typeId(this);
	}

	@Override
	public LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public O op(CodeWriter writer) {
		throw new UnsupportedOperationException();
	}

}
