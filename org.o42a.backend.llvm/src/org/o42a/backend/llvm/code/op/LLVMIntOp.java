/*
    Compiler LLVM Back-end
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.codegen.code.op.IntOp;


public abstract class LLVMIntOp<O extends IntOp<O>, T extends O>
		extends LLVMNumOp< O, T>
		implements IntOp<O> {

	public LLVMIntOp(long blockPtr, long nativePtr) {
		super(blockPtr, nativePtr);
	}

	@Override
	public T shl(Code code, O numBits) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				shl(nextPtr, getNativePtr(), nativePtr(numBits)));
	}

	@Override
	public T shl(Code code, int numBits) {
		return shl(code, constantValue(code, numBits));
	}

	@Override
	public T lshr(Code code, O numBits) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				lshr(nextPtr, getNativePtr(), nativePtr(numBits)));
	}

	@Override
	public T lshr(Code code, int numBits) {
		return lshr(code, constantValue(code, numBits));
	}

	@Override
	public T ashr(Code code, O numBits) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				ashr(nextPtr, getNativePtr(), nativePtr(numBits)));
	}

	@Override
	public T ashr(Code code, int numBits) {
		return ashr(code, constantValue(code, numBits));
	}

	@Override
	public T and(Code code, O operand) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				and(nextPtr, getNativePtr(), nativePtr(operand)));
	}

	@Override
	public T or(Code code, O operand) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				or(nextPtr, getNativePtr(), nativePtr(operand)));
	}

	@Override
	public T xor(Code code, O operand) {

		final long nextPtr = nextPtr(code);

		return create(
				nextPtr,
				xor(nextPtr, getNativePtr(), nativePtr(operand)));
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
				gt(nextPtr(code), getNativePtr(), nativePtr(other)));
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
	public LLVMFp64op toFp64(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMFp64op(
				nextPtr,
				intToFp64(nextPtr, getNativePtr()));
	}

	@Override
	public LLVMBoolOp lowestBit(Code code) {

		final long nextPtr = nextPtr(code);

		return new LLVMBoolOp(
				nextPtr,
				lowestBit(nextPtr, getNativePtr()));
	}

	protected abstract T constantValue(Code code, int value);

	private static native long shl(
			long blockPtr,
			long valuePtr,
			long numBitsPtr);

	private static native long lshr(
			long blockPtr,
			long valuePtr,
			long numBitsPtr);

	private static native long ashr(
			long blockPtr,
			long valuePtr,
			long numBitsPtr);

	private static native long and(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long or(
			long blockPtr,
			long op1ptr,
			long op2ptr);

	private static native long xor(
			long blockPtr,
			long op1ptr,
			long op2ptr);

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

	static native long eq(
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

	static native long int32to64(long blockPtr, long valuePtr);

	static native long int64to32(long blockPtr, long valuePtr);

	private static native long intToFp64(long blockPtr, long valuePtr);

	private static native long lowestBit(
			long blockPtr,
			long valuePtr);

}
