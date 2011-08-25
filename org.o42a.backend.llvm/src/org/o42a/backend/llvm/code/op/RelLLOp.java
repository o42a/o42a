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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.nextPtr;
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.StructOp;


public final class RelLLOp implements LLOp<RelOp>, RelOp {

	private final CodeId id;
	private final long blockPtr;
	private final long nativePtr;

	public RelLLOp(CodeId id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	@Override
	public long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public void allocated(Code code, StructOp<?> enclosing) {
	}

	@Override
	public AnyLLOp offset(CodeId id, Code code, PtrOp<?> from) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId;

		if (id != null) {
			resultId = code.opId(id);
		} else {
			resultId = getId().detail("offset_from").detail(from.getId());
		}

		return new AnyLLOp(
				resultId,
				AUTO_ALLOC_CLASS,
				nextPtr,
				offsetBy(
						nextPtr,
						resultId.getId(),
						nativePtr(from),
						getNativePtr()));
	}

	@Override
	public Int32llOp toInt32(CodeId id, Code code) {
		return new Int32llOp(
				code.opId(id),
				getBlockPtr(),
				getNativePtr());
	}

	@Override
	public RelLLOp create(CodeId id, long blockPtr, long nativePtr) {
		return new RelLLOp(id, blockPtr, nativePtr);
	}

	private static native long offsetBy(
			long blockPtr,
			String id,
			long fromPtr,
			long byPtr);

}
