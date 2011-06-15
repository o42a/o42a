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

import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AllocClass;


public final class LLVMAnyOp extends LLVMPtrOp implements AnyOp {

	public LLVMAnyOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public LLVMAnyOp toAny(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toAny(id, code);
	}

	@Override
	public LLVMRecOp.Any toPtr(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "any");

		return new LLVMRecOp.Any(
				resultId,
				getAllocClass(),
				nextPtr,
				toPtr(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public LLVMRecOp.Int8 toInt8(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int8");

		return new LLVMRecOp.Int8(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(nextPtr, resultId.getId(), getNativePtr(), (byte) 8));
	}

	@Override
	public LLVMRecOp.Int16 toInt16(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int16");

		return new LLVMRecOp.Int16(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(nextPtr, resultId.getId(), getNativePtr(), (byte) 16));
	}

	@Override
	public LLVMRecOp.Int32 toInt32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int32");

		return new LLVMRecOp.Int32(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(nextPtr, resultId.getId(), getNativePtr(), (byte) 32));
	}

	@Override
	public LLVMRecOp.Int64 toInt64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int64");

		return new LLVMRecOp.Int64(
				resultId,
				getAllocClass(),
				nextPtr,
				toInt(nextPtr, resultId.getId(), getNativePtr(), (byte) 64));
	}

	@Override
	public LLVMRecOp.Fp32 toFp32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp32");

		return new LLVMRecOp.Fp32(
				resultId,
				getAllocClass(),
				nextPtr,
				toFp32(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public LLVMRecOp.Fp64 toFp64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp64");

		return new LLVMRecOp.Fp64(
				resultId,
				getAllocClass(),
				nextPtr,
				toFp64(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public LLVMRecOp.Rel toRel(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "rel");

		return new LLVMRecOp.Rel(
				resultId,
				getAllocClass(),
				nextPtr,
				toRelPtr(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public LLVMAnyOp create(CodeId id, long blockPtr, long nativePtr) {
		return new LLVMAnyOp(
				id,
				getAllocClass(),
				blockPtr,
				nativePtr);
	}

}
