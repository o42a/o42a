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

import org.o42a.backend.llvm.code.op.LLVMRecOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.AllocClass;


final class RelDataAlloc extends SimpleDataAllocation<RecOp<RelOp>> {

	public RelDataAlloc(ContainerAllocation<?> enclosing) {
		super(enclosing);
	}

	@Override
	public DataLayout getLayout() {
		return getModule().dataAllocator().relPtrLayout();
	}

	@Override
	protected RecOp<RelOp> op(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		return new LLVMRecOp.Rel(id, allocClass, blockPtr, nativePtr);
	}

}
