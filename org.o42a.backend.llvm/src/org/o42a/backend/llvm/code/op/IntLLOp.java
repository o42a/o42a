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
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.IntOp;


public abstract class IntLLOp<O extends IntOp<O>, T extends O>
		extends NumLLOp<O, T>
		implements IntOp<O> {

	private final int bits;

	public IntLLOp(CodeId id, int bits, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.bits = bits;
	}

	public final int getBits() {
		return this.bits;
	}

	@Override
	public T shl(CodeId id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = unaryId(id, code, "shl");

		return create(
				resultId,
				nextPtr,
				llvm.instr(shl(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T shl(CodeId id, Code code, int numBits) {
		return shl(null, code, constantValue(code, numBits));
	}

	@Override
	public T lshr(CodeId id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "lshr", numBits);

		return create(
				resultId,
				nextPtr,
				llvm.instr(lshr(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T lshr(CodeId id, Code code, int numBits) {
		return lshr(null, code, constantValue(code, numBits));
	}

	@Override
	public T ashr(CodeId id, Code code, O numBits) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "ashr", numBits);

		return create(
				resultId,
				nextPtr,
				llvm.instr(ashr(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(numBits))));
	}

	@Override
	public T ashr(CodeId id, Code code, int numBits) {
		return ashr(null, code, constantValue(code, numBits));
	}

	@Override
	public T and(CodeId id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "and", operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(and(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T or(CodeId id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "or", operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(or(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T xor(CodeId id, Code code, O operand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "xor", operand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(xor(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(operand))));
	}

	@Override
	public T neg(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = unaryId(id, code, "neg");

		return create(
				resultId,
				nextPtr,
				llvm.instr(neg(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public T add(CodeId id, Code code, O summand) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "add", summand);

		return create(
				resultId,
				nextPtr,
				llvm.instr(add(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(summand))));
	}

	@Override
	public T sub(CodeId id, Code code, O subtrahend) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "sub", subtrahend);

		return create(
				resultId,
				nextPtr,
				llvm.instr(sub(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(subtrahend))));
	}

	@Override
	public T mul(CodeId id, Code code, O multiplier) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "mul", multiplier);

		return create(
				resultId,
				nextPtr,
				llvm.instr(mul(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(multiplier))));
	}

	@Override
	public T div(CodeId id, Code code, O divisor) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "div", divisor);

		return create(
				resultId,
				nextPtr,
				llvm.instr(div(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor))));
	}

	@Override
	public T rem(CodeId id, Code code, O divisor) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "rem", divisor);

		return create(
				resultId,
				nextPtr,
				llvm.instr(rem(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor))));
	}

	@Override
	public BoolLLOp eq(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "eq", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(eq(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp ne(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "ne", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(ne(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp gt(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "gt", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(gt(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp ge(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "ge", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(ge(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp lt(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "lt", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(lt(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public BoolLLOp le(CodeId id, Code code, O other) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = binaryId(id, code, "le", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(le(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other))));
	}

	@Override
	public Int8llOp toInt8(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "int8");

		return new Int8llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 8)));
	}

	@Override
	public Int16llOp toInt16(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "int16");

		return new Int16llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 16)));
	}

	@Override
	public Int32llOp toInt32(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "int32");

		return new Int32llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 32)));
	}

	@Override
	public Int64llOp toInt64(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "int64");

		return new Int64llOp(
				resultId,
				nextPtr,
				llvm.instr(int2int(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 64)));
	}

	@Override
	public Fp32llOp toFp32(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "fp32");

		return new Fp32llOp(
				resultId,
				nextPtr,
				llvm.instr(intToFp32(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public Fp64llOp toFp64(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "fp64");

		return new Fp64llOp(
				resultId,
				nextPtr,
				llvm.instr(intToFp64(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public BoolLLOp lowestBit(CodeId id, Code code) {

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = castId(id, code, "bool");

		return new BoolLLOp(
				resultId,
				nextPtr,
				llvm.instr(lowestBit(
						nextPtr,
						llvm.nextInstr(),
						ids.writeCodeId(resultId),
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

	private static native long ne(
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
