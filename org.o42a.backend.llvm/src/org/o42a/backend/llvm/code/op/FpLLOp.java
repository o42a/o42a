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

import static org.o42a.backend.llvm.code.LLCode.nativePtr;
import static org.o42a.backend.llvm.code.LLCode.nextPtr;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.FpOp;


public abstract class FpLLOp<O extends FpOp<O>, T extends O>
		extends NumLLOp< O, T>
		implements FpOp<O> {

	public FpLLOp(CodeId id, long blockPtr, long nativePtr) {
		super(id, blockPtr, nativePtr);
	}

	@Override
	public T neg(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = unaryId(id, code, "neg");

		return create(
				resultId,
				nextPtr,
				neg(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
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
	public BoolLLOp eq(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "eq", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				eq(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public BoolLLOp ne(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "ne", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				ne(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public BoolLLOp gt(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "gt", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				gt(
						nextPtr,
						resultId.getId(),
						getNativePtr(), nativePtr(other)));
	}

	@Override
	public BoolLLOp ge(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "ge", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				ge(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public BoolLLOp lt(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "lt", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				lt(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public BoolLLOp le(CodeId id, Code code, O other) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = binaryId(id, code, "le", other);

		return new BoolLLOp(
				resultId,
				nextPtr,
				le(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						nativePtr(other)));
	}

	@Override
	public Int8llOp toInt8(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int8");

		return new Int8llOp(
				resultId,
				nextPtr,
				fp2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 8));
	}

	@Override
	public Int16llOp toInt16(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int16");

		return new Int16llOp(
				resultId,
				nextPtr,
				fp2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 16));
	}

	@Override
	public Int32llOp toInt32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int32");

		return new Int32llOp(
				resultId,
				nextPtr,
				fp2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 32));
	}

	@Override
	public Int64llOp toInt64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "int64");

		return new Int64llOp(
				resultId,
				nextPtr,
				fp2int(
						nextPtr,
						resultId.getId(),
						getNativePtr(),
						(byte) 64));
	}

	@Override
	public Fp32llOp toFp32(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp32");

		return new Fp32llOp(
				resultId,
				nextPtr,
				fp2fp32(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
	}

	@Override
	public Fp64llOp toFp64(CodeId id, Code code) {

		final long nextPtr = nextPtr(code);
		final CodeId resultId = castId(id, code, "fp64");

		return new Fp64llOp(
				resultId,
				nextPtr,
				fp2fp64(
						nextPtr,
						resultId.getId(),
						getNativePtr()));
	}

	private static native long neg(long blockPtr, String id, long valuePtr);

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

	private static native long eq(
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

	private static native long fp2int(
			long blockPtr,
			String id,
			long valuePtr,
			byte intBits);

	private static native long fp2fp32(
			long blockPtr,
			String id,
			long valuePtr);

	private static native long fp2fp64(
			long blockPtr,
			String id,
			long valuePtr);

}
