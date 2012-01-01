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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nextPtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.rec.*;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AllocClass;


public final class AnyLLOp extends PtrLLOp<AnyOp> implements AnyOp {

	public AnyLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public AnyLLOp toAny(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toAny(id, code);
	}

	@Override
	public AnyRecLLOp toPtr(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "any");

		return new AnyRecLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toPtr(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	@Override
	public Int8recLLOp toInt8(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "int8");

		return new Int8recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 8));
	}

	@Override
	public Int16recLLOp toInt16(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "int16");

		return new Int16recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 16));
	}

	@Override
	public Int32recLLOp toInt32(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "int32");

		return new Int32recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 32));
	}

	@Override
	public Int64recLLOp toInt64(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "int64");

		return new Int64recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 64));
	}

	@Override
	public Fp32recLLOp toFp32(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "fp32");

		return new Fp32recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toFp32(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	@Override
	public Fp64recLLOp toFp64(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "fp64");

		return new Fp64recLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toFp64(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	@Override
	public RelRecLLOp toRel(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final CodeId resultId = castId(id, code, "rel");

		return new RelRecLLOp(
				resultId,
				getAllocClass(),
				nextPtr,
				toRelPtr(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	@Override
	public AnyLLOp create(CodeId id, long blockPtr, long nativePtr) {
		return new AnyLLOp(
				id,
				getAllocClass(),
				blockPtr,
				nativePtr);
	}

}
