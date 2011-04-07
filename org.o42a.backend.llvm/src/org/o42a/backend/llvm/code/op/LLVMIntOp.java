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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;


public abstract class LLVMIntOp<O extends IntOp<O>, T extends O>
		extends LLVMNumOp< O, T>
		implements IntOp<O> {

	public LLVMIntOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public T shl(String name, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId id = unaryId(name, code, "shl");

		return create(
				id,
				nextPtr,
				shl(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T shl(String name, Code code, int numBits) {
		return shl(null, code, constantValue(code, numBits));
	}

	@Override
	public T lshr(String name, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "lshr", numBits);

		return create(
				id,
				nextPtr,
				lshr(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T lshr(String name, Code code, int numBits) {
		return lshr(null, code, constantValue(code, numBits));
	}

	@Override
	public T ashr(String name, Code code, O numBits) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "ashr", numBits);

		return create(
				id,
				nextPtr,
				ashr(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(numBits)));
	}

	@Override
	public T ashr(String name, Code code, int numBits) {
		return ashr(null, code, constantValue(code, numBits));
	}

	@Override
	public T and(String name, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "and", operand);

		return create(
				id,
				nextPtr,
				and(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T or(String name, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "or", operand);

		return create(
				id,
				nextPtr,
				or(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T xor(String name, Code code, O operand) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "xor", operand);

		return create(
				id,
				nextPtr,
				xor(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(operand)));
	}

	@Override
	public T neg(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = unaryId(name, code, "neg");

		return create(
				id,
				nextPtr,
				neg(nextPtr, id.toString(), getNativePtr()));
	}

	@Override
	public T add(String name, Code code, O summand) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "add", summand);

		return create(
				id,
				nextPtr,
				add(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(summand)));
	}

	@Override
	public T sub(String name, Code code, O subtrahend) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "sub", subtrahend);

		return create(
				id,
				nextPtr,
				sub(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(subtrahend)));
	}

	@Override
	public T mul(String name, Code code, O multiplier) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "mul", multiplier);

		return create(
				id,
				nextPtr,
				mul(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(multiplier)));
	}

	@Override
	public T div(String name, Code code, O divisor) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "div", divisor);

		return create(
				id,
				nextPtr,
				div(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(divisor)));
	}

	@Override
	public T rem(String name, Code code, O divisor) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "rem", divisor);

		return create(
				id,
				nextPtr,
				rem(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(divisor)));
	}

	@Override
	public LLVMBoolOp eq(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "eq", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				eq(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ne(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "ne", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				ne(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp gt(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "gt", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				gt(
						nextPtr(code),
						id.toString(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp ge(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "ge", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				ge(
						nextPtr,
						id.toString(),
						getNativePtr(), nativePtr(other)));
	}

	@Override
	public LLVMBoolOp lt(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);

		final CodeId id = binaryId(name, code, "lt", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				lt(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMBoolOp le(String name, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId id = binaryId(name, code, "le", other);

		return new LLVMBoolOp(
				id,
				nextPtr,
				le(
						nextPtr,
						id.toString(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public LLVMInt8op toInt8(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "int8");

		return new LLVMInt8op(
				id,
				nextPtr,
				int2int(
						nextPtr,
						id.toString(),
						getNativePtr(),
						(byte) 8));
	}

	@Override
	public LLVMInt16op toInt16(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "int16");

		return new LLVMInt16op(
				id,
				nextPtr,
				int2int(
						nextPtr,
						id.toString(),
						getNativePtr(),
						(byte) 16));
	}

	@Override
	public LLVMInt32op toInt32(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "int32");

		return new LLVMInt32op(
				id,
				nextPtr,
				int2int(
						nextPtr,
						id.toString(),
						getNativePtr(),
						(byte) 32));
	}

	@Override
	public LLVMInt64op toInt64(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "int64");

		return new LLVMInt64op(
				id,
				nextPtr,
				int2int(
						nextPtr,
						id.toString(),
						getNativePtr(),
						(byte) 64));
	}

	@Override
	public LLVMFp32op toFp32(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "fp32");

		return new LLVMFp32op(
				id,
				nextPtr,
				intToFp32(
						nextPtr,
						id.toString(),
						getNativePtr()));
	}

	@Override
	public LLVMFp64op toFp64(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "fp64");

		return new LLVMFp64op(
				id,
				nextPtr,
				intToFp64(
						nextPtr,
						id.toString(),
						getNativePtr()));
	}

	@Override
	public LLVMBoolOp lowestBit(String name, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId id = castId(name, code, "bool");

		return new LLVMBoolOp(
				id,
				nextPtr,
				lowestBit(
						nextPtr,
						id.toString(),
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

}
