/*
    Compiler LLVM Back-end
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.backend.llvm.code.LLVMCode.nativePtr;
import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.FpOp;


public abstract class LLVMFpOp<O extends FpOp<O>, T extends O>
		extends LLVMNumOp< O, T>
		implements FpOp<O> {

	public LLVMFpOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public T neg(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return create(
				unaryId(name, code, "neg"),
				nextPtr,
				neg(nextPtr, getNativePtr()));
	}

	@Override
	public T add(String name, Code code, O summand) {

		final long nextPtr = nextPtr(code);

		return create(
				binaryId(name, code, "add", summand),
				nextPtr,
				add(nextPtr, getNativePtr(), nativePtr(summand)));
	}

	@Override
	public T sub(String name, Code code, O subtrahend) {

		final long nextPtr = nextPtr(code);

		return create(
				binaryId(name, code, "sub", subtrahend),
				nextPtr, sub(nextPtr, getNativePtr(), nativePtr(subtrahend)));
	}

	@Override
	public T mul(String name, Code code, O multiplier) {

		final long nextPtr = nextPtr(code);

		return create(
				binaryId(name, code, "mul", multiplier),
				nextPtr, mul(nextPtr, getNativePtr(), nativePtr(multiplier)));
	}

	@Override
	public T div(String name, Code code, O divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				binaryId(name, code, "div", divisor),
				nextPtr, div(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public T rem(String name, Code code, O divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				binaryId(name, code, "rem", divisor),
				nextPtr, rem(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public LLVMBoolOp eq(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "eq", other),
				nextPtr,
				eq(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ne(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "ne", other),
				nextPtr,
				ne(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp gt(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "gt", other),
				nextPtr,
				gt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ge(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "ge", other),
				nextPtr,
				ge(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp lt(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "lt", other),
				nextPtr,
				lt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp le(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				binaryId(name, code, "le", other),
				nextPtr,
				le(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMInt8op toInt8(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt8op(
				castId(name, code, "int8"),
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 8));
	}

	@Override
	public LLVMInt16op toInt16(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt16op(
				castId(name, code, "int16"),
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 16));
	}

	@Override
	public LLVMInt32op toInt32(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt32op(
				castId(name, code, "int32"),
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 32));
	}

	@Override
	public LLVMInt64op toInt64(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt64op(
				castId(name, code, "int64"),
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 64));
	}

	@Override
	public LLVMFp32op toFp32(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp32op(
				castId(name, code, "fp32"),
				nextPtr,
				fp2fp32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMFp64op toFp64(String name, Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp64op(
				castId(name, code, "fp64"),
				nextPtr,
				fp2fp64(nextPtr, getNativePtr()));
	}

	private static native long neg(long blockPtr, long valuePtr);

	private static native long add(long blockPtr, long op1ptr, long op2ptr);

	private static native long sub(long blockPtr, long op1ptr, long op2ptr);

	private static native long mul(long blockPtr, long op1ptr, long op2ptr);

	private static native long div(long blockPtr, long op1ptr, long op2ptr);

	private static native long rem(long blockPtr, long op1ptr, long op2ptr);

	private static native long eq(long blockPtr, long op1ptr, long op2ptr);

	private static native long ne(long blockPtr, long op1ptr, long op2ptr);

	private static native long gt(long blockPtr, long op1ptr, long op2ptr);

	private static native long ge(long blockPtr, long op1ptr, long op2ptr);

	private static native long lt(long blockPtr, long op1ptr, long op2ptr);

	private static native long le(long blockPtr, long op1ptr, long op2ptr);

	private static native long fp2int(
			long blockPtr,
			long valuePtr,
			byte intBits);

	private static native long fp2fp32(long blockPtr, long valuePtr);

	private static native long fp2fp64(long blockPtr, long valuePtr);

}
