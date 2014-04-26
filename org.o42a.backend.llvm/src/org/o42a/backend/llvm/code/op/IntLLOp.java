/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.util.string.ID;


public abstract class IntLLOp<O extends IntOp<O>, T extends O>
		extends NumLLOp<O, T>
		implements IntOp<O> {

	private final int bits;

	public IntLLOp(ID id, int bits, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.bits = bits;
	}

	public final int getBits() {
		return this.bits;
	}

	@Override
	public T shl(ID id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, SHL_ID, this, numBits);

		return create(
				resultId,
				nextPtr,
				llvm.instr(shl(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T shl(ID id, Code code, int numBits) {
		return shl(
				code.getOpNames().binaryId(id, SHL_ID, this, numBits),
				code,
				constantValue(code, numBits));
	}

	@Override
	public T lshr(ID id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, LSHR_ID, this, numBits);

		return create(
				resultId,
				nextPtr,
				llvm.instr(lshr(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T lshr(ID id, Code code, int numBits) {
		return lshr(
				code.getOpNames().binaryId(id, LSHR_ID, this, numBits),
				code,
				constantValue(code, numBits));
	}

	@Override
	public T ashr(ID id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, ASHR_ID, this, numBits);

		return create(
				resultId,
				nextPtr,
				llvm.instr(ashr(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T ashr(ID id, Code code, int numBits) {
		return ashr(
				code.getOpNames().binaryId(id, ASHR_ID, this, numBits),
				code,
				constantValue(code, numBits));
	}

	@Override
	public T and(ID id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, AND_ID, this, operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(and(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T or(ID id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, OR_ID, this, operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(or(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T xor(ID id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, XOR_ID, this, operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(xor(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T neg(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().unaryId(id, NEG_ID, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(neg(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public T add(ID id, Code code, O summand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, ADD_ID, summand, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(add(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(summand))));
	}

	@Override
	public T sub(ID id, Code code, O subtrahend) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, SUB_ID, subtrahend, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(sub(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(subtrahend))));
	}

	@Override
	public T mul(ID id, Code code, O multiplier) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, MUL_ID, multiplier, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(mul(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(multiplier))));
	}

	@Override
	public T div(ID id, Code code, O divisor) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, DIV_ID, divisor, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(div(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor))));
	}

	@Override
	public T rem(ID id, Code code, O divisor) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId =
				code.getOpNames().binaryId(id, REM_ID, divisor, this);

		return create(
				resultId,
				nextPtr,
				llvm.instr(rem(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor))));
	}

	@Override
	public BoolLLOp eq(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, EQ_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(eq(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp ne(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, NE_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(ne(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp gt(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, GT_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(gt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp ge(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, GE_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(ge(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp lt(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, LT_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(lt(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp le(ID id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().binaryId(id, LE_ID, other, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(le(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public Int8llOp toInt8(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, INT8_ID, this);

		return new Int8llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 8)));
	}

	@Override
	public Int16llOp toInt16(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, INT16_ID, this);

		return new Int16llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 16)));
	}

	@Override
	public Int32llOp toInt32(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, INT32_ID, this);

		return new Int32llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 32)));
	}

	@Override
	public Int64llOp toInt64(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, INT64_ID, this);

		return new Int64llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 64)));
	}

	@Override
	public Fp32llOp toFp32(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, FP32_ID, this);

		return new Fp32llOp(
				resultId,
				nextPtr,
				llvm.instr(intToFp32(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public Fp64llOp toFp64(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, FP64_ID, this);

		return new Fp64llOp(
				resultId,
				nextPtr,
				llvm.instr(intToFp64(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public BoolLLOp lowestBit(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final ID resultId = code.getOpNames().castId(id, BOOL_ID, this);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(lowestBit(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

	protected abstract T constantValue(Code code, int value);

	private static native long shl(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr,
			long numBitsPtr);

	private static native long lshr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr,
			long numBitsPtr);

	private static native long ashr(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr,
			long numBitsPtr);

	private static native long and(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long or(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long xor(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long neg(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long add(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long sub(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long mul(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long div(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long rem(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	static native long eq(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	static native long ne(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long gt(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long ge(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long lt(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long le(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long int2int(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr,
			byte intBits);

	private static native long intToFp32(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long intToFp64(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long lowestBit(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

}
