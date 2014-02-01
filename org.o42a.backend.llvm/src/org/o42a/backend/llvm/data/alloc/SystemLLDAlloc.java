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

import org.o42a.backend.llvm.code.op.SystemLLOp;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public class SystemLLDAlloc extends SimpleLLDAlloc<SystemOp> {

	private final SystemTypeLLAlloc typeAlloc;

	public SystemLLDAlloc(
			ContainerLLDAlloc<?> enclosing,
			DataAllocation<SystemOp> proto,
			SystemTypeLLAlloc typeAlloc) {
		super(enclosing, proto);
		this.typeAlloc = typeAlloc;
		init();
	}

	public final SystemTypeLLAlloc getTypeAlloc() {
		return this.typeAlloc;
	}

	@Override
	public final DataLayout getLayout() {
		return this.typeAlloc.getLayout();
	}

	@Override
	protected SystemLLOp op(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		return new SystemLLOp(
				id,
				allocPlace,
				blockPtr,
				nativePtr,
				this.typeAlloc);
	}

}
