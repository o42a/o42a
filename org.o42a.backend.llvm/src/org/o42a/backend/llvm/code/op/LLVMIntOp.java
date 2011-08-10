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

import org.o42a.backend.llvm.code.LLVMCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.RecOp;


public abstract class LLVMIntOp<O extends IntOp<O>, T extends O>
		extends LLVMNumOp<O, T>
		implements IntOp<O> {

	private final int bits;

	public LLVMIntOp(CodeId id, int bits, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.bits = bits;
	}

	public final int getBits() {
		return this.bits;
	}

	@Override
	public T shl(CodeId id, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = unaryId(id, code, "shl");

		return create(
				resultId,
				nextPtr,
				shl(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T shl(CodeId id, Code code, int numBits) {
		return shl(null, code, constantValue(code, numBits));
	}

	@Override
	public T lshr(CodeId id, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "lshr", numBits);

		return create(
				resultId,
				nextPtr,
				lshr(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T lshr(CodeId id, Code code, int numBits) {
		return lshr(null, code, constantValue(code, numBits));
	}

	@Override
	public T ashr(CodeId id, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "ashr", numBits);

		return create(
				resultId,
				nextPtr,
				ashr(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T ashr(CodeId id, Code code, int numBits) {
		return ashr(null, code, constantValue(code, numBits));
	}

	@Override
	public T and(CodeId id, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "and", operand);

		return create(
				resultId,
				nextPtr,
				and(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T or(CodeId id, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "or", operand);

		return create(
				resultId,
				nextPtr,
				or(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T xor(CodeId id, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "xor", operand);

		return create(
				resultId,
				nextPtr,
				xor(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T neg(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = unaryId(id, code, "neg");

		return create(
				resultId,
				nextPtr,
				neg(nextPtr, resultId.getId(), getNativePtr()));
	}

	@Override
	public T add(CodeId id, Code code, O summand) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "add", summand);

		return create(
				resultId,
				nextPtr,
				add(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(summand)));
	}

	@Override
	public O atomicAdd(CodeId id, Code code, RecOp<?, O> to) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId =
				LLVMCode.binaryId(to, id, code, "atomic_add", this);
		final String op =
				"llvm.atomic.load.add.i" + getBits() + ".p0i" + getBits();

		return create(
				resultId,
				nextPtr,
				atomicBinary(
						nextPtr,
						resultId.getId(),
						op,
						nativePtr(to),
						getNativePtr(),
						getBits()));
	}

	@Override
	public T sub(CodeId id, Code code, O subtrahend) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "sub", subtrahend);

		return create(
				resultId,
				nextPtr,
				sub(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(subtrahend)));
	}

	@Override
	public O atomicSub(CodeId id, Code code, RecOp<?, O> from) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId =
				LLVMCode.binaryId(from, id, code, "atomic_sub", this);
		final String op =
				"llvm.atomic.load.sub.i" + getBits() + ".p0i" + getBits();

		return create(
				resultId,
				nextPtr,
				atomicBinary(
						nextPtr,
						resultId.getId(),
						op,
						nativePtr(from),
						getNativePtr(),
						getBits()));
	}

	@Override
	public T mul(CodeId id, Code code, O multiplier) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "mul", multiplier);

		return create(
				resultId,
				nextPtr,
				mul(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(multiplier)));
	}

	@Override
	public T div(CodeId id, Code code, O divisor) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "div", divisor);

		return create(
				resultId,
				nextPtr,
				div(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(divisor)));
	}

	@Override
	public T rem(CodeId id, Code code, O divisor) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "rem", divisor);

		return create(
				resultId,
				nextPtr,
				rem(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(divisor)));
	}

	@Override
	public LLVMBoolOp eq(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "eq", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				eq(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ne(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "ne", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				ne(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp gt(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "gt", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				gt(
						nextPtr(code),
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ge(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "ge", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				ge(
						nextPtr,
						resultId.getId(),
						getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp lt(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "lt", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				lt(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp le(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "le", other);

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				le(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMInt8op toInt8(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int8");

		return new LLVMInt8op(
				resultId,
				nextPtr,
				int2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 8));
	}

	@Override
	public LLVMInt16op toInt16(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int16");

		return new LLVMInt16op(
				resultId,
				nextPtr,
				int2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 16));
	}

	@Override
	public LLVMInt32op toInt32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int32");

		return new LLVMInt32op(
				resultId,
				nextPtr,
				int2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 32));
	}

	@Override
	public LLVMInt64op toInt64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int64");

		return new LLVMInt64op(
				resultId,
				nextPtr,
				int2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 64));
	}

	@Override
	public LLVMFp32op toFp32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp32");

		return new LLVMFp32op(
				resultId,
				nextPtr,
				intToFp32(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
	}

	@Override
	public LLVMFp64op toFp64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp64");

		return new LLVMFp64op(
				resultId,
				nextPtr,
				intToFp64(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
	}

	@Override
	public LLVMBoolOp lowestBit(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "bool");

		return new LLVMBoolOp(
				resultId,
				nextPtr,
				lowestBit(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
	}

	protected abstract T constantValue(Code code, int value);

	private static native long shl(
			long blockPtr,
			String id,
			long valuePtr,
			long numBitsPtr);

	private static native long lshr(
			long blockPtr,
			String id,
			long valuePtr,
			long numBitsPtr);

	private static native long ashr(
			long blockPtr,
			String id,
			long valuePtr,
			long numBitsPtr);

	private static native long and(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long or(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long xor(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long neg(
			long blockPtr,
			String id,
			long valuePtr);

	private static native long add(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long sub(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long mul(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long div(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long rem(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	static native long eq(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long ne(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long gt(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long ge(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long lt(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long le(
			long blockPtr,
			String id,
			long op1ptr,
			long op2ptr);

	private static native long int2int(
			long blockPtr,
			String id,
			long valuePtr,
			byte intBits);

	private static native long intToFp32(
			long blockPtr,
			String id,
			long valuePtr);

	private static native long intToFp64(
			long blockPtr,
			String id,
			long valuePtr);

	private static native long lowestBit(
			long blockPtr,
			String id,
			long valuePtr);

	private static native long atomicBinary(
			long blockPtr,
			String id,
			String op,
			long targetPtr,
			long operandPtr,
			int bits);

}
