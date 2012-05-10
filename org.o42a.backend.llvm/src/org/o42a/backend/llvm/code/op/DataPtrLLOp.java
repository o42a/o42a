/*
    Compiler LLVM Back-end
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.typePtr;
import static org.o42a.codegen.data.AllocClass.DEFAULT_ALLOC_CLASS;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public abstract class DataPtrLLOp<P extends DataPtrOp<P>>
		extends PtrLLOp<P>
		implements DataPtrOp<P> {

	private final AllocClass allocClass;

	public DataPtrLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.allocClass = allocClass != null ? allocClass : DEFAULT_ALLOC_CLASS;
	}

	@Override
	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	@Override
	public P offset(CodeId id, Code code, IntOp<?> index) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId offsetId = code.getOpNames().indexId(id, this, index);

		return create(
				offsetId,
				nextPtr,
				llvm.instr(offset(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(offsetId),
						ids.length(),
						getNativePtr(),
						nativePtr(index))));
	}

	@Override
	public AnyLLOp toAny(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = code.getOpNames().castId(id, "any", this);

		return new AnyLLOp(
				castId,
				getAllocClass(),
				nextPtr,
				llvm.instr(toAny(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr())));
	}

	public DataLLOp toData(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = code.getOpNames().castId(id, "data", this);

		return new DataLLOp(
				castId,
				getAllocClass(),
				nextPtr,
				llvm.instr(toAny(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr())));
	}

	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId castId = code.getOpNames().castId(id, type.getId(), this);

		return type.op(new LLStruct<SS>(
				castId,
				getAllocClass(),
				type,
				nextPtr,
				llvm.instr(castStructTo(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(castId),
						ids.length(),
						getNativePtr(),
						typePtr(type)))));
	}

}
