/*
    Compiler LLVM Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.codegen.code.op.FpOp;
import org.o42a.util.string.ID;


public abstract class FpLLOp<O extends FpOp<O>, T extends O>
		extends NumLLOp< O, T>
		implements FpOp<O> {

	public FpLLOp(ID id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
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
				code.getOpNames().binaryId(id, ADD_ID, this, summand);

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
				code.getOpNames().binaryId(id, SUB_ID, this, subtrahend);

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
				code.getOpNames().binaryId(id, MUL_ID, this, multiplier);

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
				code.getOpNames().binaryId(id, DIV_ID, this, divisor);

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
				code.getOpNames().binaryId(id, REM_ID, this, divisor);

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
		final ID resultId = code.getOpNames().binaryId(id, EQ_ID, this, other);

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
		final ID resultId = code.getOpNames().binaryId(id, NE_ID, this, other);

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
		final ID resultId = code.getOpNames().binaryId(id, GT_ID, this, other);

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
		final ID resultId = code.getOpNames().binaryId(id, GE_ID, this, other);

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
		final ID resultId = code.getOpNames().binaryId(id, LT_ID, this, other);

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
		final ID resultId = code.getOpNames().binaryId(id, LE_ID, this, other);

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
				llvm.instr(fp2int(
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
				llvm.instr(fp2int(
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
				llvm.instr(fp2int(
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
				llvm.instr(fp2int(
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
				llvm.instr(fp2fp32(
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
				llvm.instr(fp2fp64(
						nextPtr,
						llvm.nextInstr(),
						ids.write(resultId),
						ids.length(),
						getNativePtr())));
	}

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

	private static native long eq(
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

	private static native long fp2int(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr,
			byte intBits);

	private static native long fp2fp32(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long fp2fp64(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long valuePtr);

}
