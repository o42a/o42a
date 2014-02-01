/*
    Compiler LLVM Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.backend.llvm.id.LLVMId.systemTypeId;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.SystemType;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public class SystemTypeLLAlloc extends LLDAlloc<SystemOp> {

	private final SystemType systemType;
	private final DataLayout layout;
	private final long typePtr;
	private final LLVMId llvmId;

	public SystemTypeLLAlloc(
			LLVMModule module,
			SystemType systemType,
			DataLayout layout,
			long typePtr) {
		super(module, null);
		this.systemType = systemType;
		this.layout = layout;
		this.typePtr = typePtr;
		this.llvmId = systemTypeId(this);
	}

	public final SystemType getSystemType() {
		return this.systemType;
	}

	public final long getTypePtr() {
		assert exists() :
			"System type " + this + " does not exist";
		return this.typePtr;
	}

	@Override
	public final LLVMId llvmId() {
		return this.llvmId;
	}

	@Override
	public final DataLayout getLayout() {
		return this.layout;
	}

	public final boolean exists() {
		return this.typePtr != 0L;
	}

	@Override
	public SystemOp op(ID id, AllocClass allocClass, CodeWriter writer) {
		throw new UnsupportedOperationException();
	}

}
