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

import static org.o42a.backend.llvm.code.LLCode.llvm;
import static org.o42a.backend.llvm.code.LLCode.nativePtr;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.data.NativeBuffer;
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

		final LLCode llvm = llvm(code);
		final long nextPtr = llvm.nextPtr();
		final NativeBuffer ids = llvm.getModule().ids();
		final CodeId resultId = unaryId(id, code, "neg");

		return create(
				resultId,
				nextPtr,
				neg(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
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
				add(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(summand)));
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
				sub(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(subtrahend)));
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
				mul(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(multiplier)));
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
				div(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor)));
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
				rem(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(divisor)));
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
				eq(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
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
				ne(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
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
				gt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(), nativePtr(other)));
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
				ge(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
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
				lt(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
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
				le(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						nativePtr(other)));
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
				fp2int(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 8));
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
				fp2int(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 16));
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
				fp2int(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 32));
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
				fp2int(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr(),
						(byte) 64));
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
				fp2fp32(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
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
				fp2fp64(
						nextPtr,
						ids.writeCodeId(resultId),
						ids.length(),
						getNativePtr()));
	}

	private static native long neg(
			long blockPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long add(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long sub(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long mul(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long div(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long rem(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long eq(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long ne(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long gt(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long ge(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long lt(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long le(
			long blockPtr,
			long id,
			int idLen,
			long op1ptr,
			long op2ptr);

	private static native long fp2int(
			long blockPtr,
			long id,
			int idLen,
			long valuePtr,
			byte intBits);

	private static native long fp2fp32(
			long blockPtr,
			long id,
			int idLen,
			long valuePtr);

	private static native long fp2fp64(
			long blockPtr,
			long id,
			int idLen,
			long valuePtr);

}
