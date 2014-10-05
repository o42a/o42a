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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.typePtr;
import static org.o42a.codegen.data.AllocPlace.defaultAllocPlace;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public abstract class DataPtrLLOp<P extends DataPtrOp<P>>
		extends PtrLLOp<P>
		implements DataPtrOp<P> {

	private final AllocPlace allocPlace;

	public DataPtrLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.allocPlace = allocPlace != null ? allocPlace : defaultAllocPlace();
	}

	@Override
	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	@Override
	public P offset(ID id, Code code, IntOp<?> index) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID offsetId = code.opNames().indexId(id, this, index);

		return create(
				offsetId,
				nextPtr,
				llvm.instr(offset(
						nextPtr,
						llvm.nextInstr(),
						ids.write(offsetId),
						ids.length(),
						getNativePtr(),
						nativePtr(index))));
	}

	@Override
	public AnyLLOp toAny(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID castId = code.opNames().castId(id, ANY_ID, this);

		return new AnyLLOp(
				castId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(toAny(
						nextPtr,
						llvm.nextInstr(),
						ids.write(castId),
						ids.length(),
						getNativePtr())));
	}

	public DataLLOp toData(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID castId = code.opNames().castId(id, DATA_ID, this);

		return new DataLLOp(
				castId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(toAny(
						nextPtr,
						llvm.nextInstr(),
						ids.write(castId),
						ids.length(),
						getNativePtr())));
	}

	public <SS extends StructOp<SS>> SS to(
			ID id,
			Code code,
			Type<SS> type) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID castId = code.opNames().castId(id, type.getId(), this);

		return type.op(new LLStruct<>(
				castId,
				getAllocPlace(),
				type,
				nextPtr,
				llvm.instr(castStructTo(
						nextPtr,
						llvm.nextInstr(),
						ids.write(castId),
						ids.length(),
						getNativePtr(),
						typePtr(type)))));
	}

}
