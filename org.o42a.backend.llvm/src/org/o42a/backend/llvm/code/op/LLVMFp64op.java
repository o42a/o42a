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

import static org.o42a.backend.llvm.code.LLVMCode.nativePtr;
import static org.o42a.backend.llvm.code.LLVMCode.nextPtr;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Fp64op;


public final class LLVMFp64op extends LLVMNumOp<Fp64op, LLVMFp64op>
		implements Fp64op {

	public LLVMFp64op(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public LLVMFp64op neg(Code code) {

		final long nextPtr = nextPtr(code);

		return create(nextPtr, neg(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMFp64op add(Code code, Fp64op summand) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				add(nextPtr, getNativePtr(), nativePtr(summand)));
	}

	@Override
	public LLVMFp64op sub(Code code, Fp64op subtrahend) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				sub(nextPtr, getNativePtr(), nativePtr(subtrahend)));
	}

	@Override
	public LLVMFp64op mul(Code code, Fp64op multiplier) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				mul(nextPtr, getNativePtr(), nativePtr(multiplier)));
	}

	@Override
	public LLVMFp64op div(Code code, Fp64op divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				div(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public LLVMFp64op rem(Code code, Fp64op divisor) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				rem(nextPtr, getNativePtr(), nativePtr(divisor)));
	}

	@Override
	public LLVMBoolOp eq(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				eq(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ne(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				ne(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp gt(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				gt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ge(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				ge(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp lt(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				lt(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp le(Code code, Fp64op other) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				le(nextPtr, getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMInt32op toInt32(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt32op(
				nextPtr,
				fp64toInt32(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMInt64op toInt64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMInt64op(
				nextPtr,
				fp64toInt64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMFp64op toFp64(Code code) {
		return this;
	}

	@Override
	public LLVMFp64op create(long blockPtr, long nativePtr) {
		return new LLVMFp64op(blockPtr, nativePtr);
	}

	private static native long neg(long blockPtr, long valuePtr);

	private static native long add(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long sub(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long mul(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long div(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long rem(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long eq(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long ne(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long gt(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long ge(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long lt(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long le(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long fp64toInt32(long blockPtr, long valuePtr);

	private static native long fp64toInt64(long blockPtr, long valuePtr);

}
