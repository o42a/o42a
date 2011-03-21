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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.FpOp;


public abstract class LLVMFpOp<O extends FpOp<O>, T extends O>
		extends LLVMNumOp< O, T>
		implements FpOp<O> {

	public LLVMFpOp(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public T neg(Code code) {

		final long nextPtr = nextPtr(code);

		return create(nextPtr, neg(nextPtr, getNativePtr()));
	}

	@Override
	public T add(Code code, O summand) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				add(nextPtr, getNativePtr(), nativePtr(summand)));
	}

	@Override
	public T sub(Code code, O subtrahend) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				sub(nextPtr, getNativePtr(), nativePtr(subtrahend)));
	}

	@Override
	public T mul(Code code, O multiplier) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				mul(nextPtr, getNativePtr(), nativePtr(multiplier)));
	}

	@Override
	public T div(Code code, O divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				div(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public T rem(Code code, O divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				rem(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public LLVMBoolOp eq(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				eq(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ne(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				ne(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp gt(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				gt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ge(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				ge(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp lt(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				lt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp le(Code code, O other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				le(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMInt8op toInt8(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt8op(
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 8));
	}

	@Override
	public LLVMInt16op toInt16(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt16op(
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 16));
	}

	@Override
	public LLVMInt32op toInt32(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt32op(
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 32));
	}

	@Override
	public LLVMInt64op toInt64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt64op(
				nextPtr,
				fp2int(nextPtr, getNativePtr(), (byte) 64));
	}

	@Override
	public LLVMFp32op toFp32(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp32op(
				nextPtr,
				fp2fp32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMFp64op toFp64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp64op(
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
