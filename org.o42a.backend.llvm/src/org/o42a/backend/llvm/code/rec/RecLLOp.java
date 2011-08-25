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
package org.o42a.backend.llvm.code.rec;

import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.nextPtr;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;


public abstract class RecLLOp<R extends RecOp<R, O>, O extends Op>
		extends PtrLLOp<R>
		implements RecOp<R, O> {

	public RecLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public final O load(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = derefId(id, code);

		return createLoaded(
				resultId,
				nextPtr,
				load(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public final void store(Code code, O value) {
		store(nextPtr(code), getNativePtr(), nativePtr(value));
	}

	protected abstract O createLoaded(CodeId id, long blockPtr, long nativePtr);

}
