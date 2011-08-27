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
package org.o42a.backend.llvm.id;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.alloc.TypeLLAlloc;


final class TypeLLVMId extends TopLevelLLVMId {

	private final TypeLLAlloc<?> typeAllocation;
	private long nativePtr;

	TypeLLVMId(TypeLLAlloc<?> typeAllocation) {
		super(
				typeAllocation.getType().codeId(
						typeAllocation.getType().getGenerator()),
				LLVMIdKind.TYPE);
		this.typeAllocation = typeAllocation;
	}

	@Override
	public String toString() {
		return getGlobalId().toString();
	}

	@Override
	public long expression(LLVMModule module) {
		if (this.nativePtr != 0L) {
			return this.nativePtr;
		}
		return this.nativePtr =
				typeExpression(this.typeAllocation.getTypePtr());
	}

	@Override
	public long typeExpression(LLVMModule module) {
		return expression(module);
	}

}
