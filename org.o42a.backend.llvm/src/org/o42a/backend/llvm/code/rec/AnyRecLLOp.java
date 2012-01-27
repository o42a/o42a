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
package org.o42a.backend.llvm.code.rec;

import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.AnyLLOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.data.AllocClass;


public final class AnyRecLLOp
		extends RecLLOp<AnyRecOp, AnyOp>
		implements AnyRecOp {

	public AnyRecLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public AnyRecLLOp create(CodeId id, long blockPtr, long nativePtr) {
		return new AnyRecLLOp(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
	}

	@Override
	protected AnyOp createLoaded(CodeId id, long blockPtr, long nativePtr) {
		return new AnyLLOp(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
	}

}
