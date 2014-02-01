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

import static org.o42a.backend.llvm.id.LLVMId.typeId;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public final class TypeLLAlloc<S extends StructOp<S>>
		extends ContainerLLDAlloc<S> {

	private final LLVMId llvmId;

	public TypeLLAlloc(
			LLVMModule module,
			long typePtr,
			long typeDataPtr,
			Type<S> type) {
		super(module, typePtr, typeDataPtr, null, type);
		this.llvmId = typeId(this);
	}

	@Override
	public LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public S op(ID id, AllocClass allocClass, CodeWriter writer) {
		throw new UnsupportedOperationException();
	}

}
