/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.rec.*;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public final class AnyLLOp extends DataPtrLLOp<AnyOp> implements AnyOp {

	public AnyLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		super(id, allocPlace, blockPtr, nativePtr);
	}

	@Override
	public AnyLLOp toAny(ID id, Code code) {

		final long nextPtr = llvm(code).nextPtr();

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toAny(id, code);
	}

	@Override
	public AnyRecLLOp toPtr(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, ANY_ID, this);

		return new AnyRecLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toPtr(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public Int8recLLOp toInt8(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, INT8_ID, this);

		return new Int8recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toInt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 8)));
	}

	@Override
	public Int16recLLOp toInt16(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, INT16_ID, this);

		return new Int16recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toInt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 16)));
	}

	@Override
	public Int32recLLOp toInt32(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, INT32_ID, this);

		return new Int32recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toInt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 32)));
	}

	@Override
	public Int64recLLOp toInt64(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, INT64_ID, this);

		return new Int64recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toInt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 64)));
	}

	@Override
	public Fp32recLLOp toFp32(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, FP32_ID, this);

		return new Fp32recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toFp32(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public Fp64recLLOp toFp64(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, FP64_ID, this);

		return new Fp64recLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toFp64(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public RelRecLLOp toRel(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID resultId = code.getOpNames().castId(id, REL_ID, this);

		return new RelRecLLOp(
				resultId,
				getAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toRelPtr(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public AnyLLOp create(ID id, long blockPtr, long nativePtr) {
		return new AnyLLOp(id, null, blockPtr, nativePtr);
	}

}
