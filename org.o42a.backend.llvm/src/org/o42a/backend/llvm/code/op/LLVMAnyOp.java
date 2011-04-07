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
import org.o42a.codegen.code.op.*;


public final class LLVMAnyOp extends LLVMPtrOp implements AnyOp {

	public LLVMAnyOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public LLVMAnyOp toAny(String name, Code code) {

		final long nextPtr = nextPtr(code);

		if (nextPtr == getBlockPtr()) {
			return this;
		}

		return super.toAny(name, code);
	}

	@Override
	public LLVMRecOp<AnyOp> toPtr(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Any(
				castId(name, code, "any"),
				nextPtr,
				toPtr(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<Int8op> toInt8(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int8(
				castId(name, code, "int8"),
				nextPtr,
				toInt(nextPtr, getNativePtr(), (byte) 8));
	}

	@Override
	public LLVMRecOp<Int16op> toInt16(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int16(
				castId(name, code, "int16"),
				nextPtr,
				toInt(nextPtr, getNativePtr(), (byte) 16));
	}

	@Override
	public LLVMRecOp<Int32op> toInt32(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int32(
				castId(name, code, "int32"),
				nextPtr,
				toInt(nextPtr, getNativePtr(), (byte) 32));
	}

	@Override
	public LLVMRecOp<Int64op> toInt64(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Int64(
				castId(name, code, "int64"),
				nextPtr,
				toInt(nextPtr, getNativePtr(), (byte) 64));
	}

	@Override
	public LLVMRecOp<Fp32op> toFp32(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp32(
				castId(name, code, "fp32"),
				nextPtr,
				toFp32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<Fp64op> toFp64(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Fp64(
				castId(name, code, "fp64"),
				nextPtr,
				toFp64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMRecOp<RelOp> toRel(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMRecOp.Rel(
				castId(name, code, "rel"),
				nextPtr,
				toRelPtr(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMAnyOp create(CodeId id, long blockPtr, long nativePtr) {
		return new LLVMAnyOp(id, blockPtr, nativePtr);
	}

}
