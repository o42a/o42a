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
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public class LLFunc<F extends Func<F>> extends PtrLLOp<F>
		implements FuncCaller<F> {

	private final Signature<F> signature;

	public LLFunc(
			ID id,
			Signature<F> signature,
			long blockPtr,
			long nativePtr) {
		super(id, blockPtr, nativePtr);
		this.signature = signature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public AnyLLOp toAny(ID id, Code code) {

		final LLCode llvm = llvm(code);
		final NativeBuffer ids = llvm.getModule().ids();
		final long nextPtr = llvm.nextPtr();
		final ID castId = code.getOpNames().castId(id, ANY_ID, this);

		return new AnyLLOp(
				castId,
				constantAllocPlace(),
				nextPtr,
				llvm.instr(nextPtr, toAny(
						nextPtr,
						llvm.nextInstr(),
						ids.write(castId),
						ids.length(),
						getNativePtr())));
	}

	@Override
	public F create(ID id, long blockPtr, long nativePtr) {
		return getSignature().op(
				new LLFunc<>(id, this.signature, blockPtr, getNativePtr()));
	}

	@Override
	public void call(Code code, Op... args) {
		call(null, llvm(code), args);
	}

	@Override
	public Int8op callInt8(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Int8llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public Int16op callInt16(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Int16llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public Int32op callInt32(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Int32llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public Int64op callInt64(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Int64llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public Fp32op callFp32(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Fp32llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public Fp64op callFp64(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new Fp64llOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public BoolOp callBool(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new BoolLLOp(id, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public AnyOp callAny(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new AnyLLOp(id, null, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public DataOp callData(ID id, Code code, Op... args) {

		final LLCode llvm = llvm(code);

		return new DataLLOp(id, null, llvm.nextPtr(), call(id, llvm, args));
	}

	@Override
	public <S extends StructOp<S>> S callPtr(
			ID id,
			Code code,
			Type<S> type,
			Op... args) {

		final LLCode llvm = llvm(code);

		return type.op(new LLStruct<>(
				id,
				null,
				type,
				llvm.nextPtr(),
				call(id, llvm, args)));
	}

	private long call(ID id, LLCode code, Op[] args) {

		final NativeBuffer ids = code.getModule().ids();
		final long[] argPtrs = new long[args.length];

		for (int i = 0; i < args.length; ++i) {
			argPtrs[i] = nativePtr(args[i]);
		}

		final long nextPtr = code.nextPtr();

		return code.instr(
				nextPtr,
				call(
						nextPtr,
						code.nextInstr(),
						ids.write(id),
						ids.length(),
						getNativePtr(),
						argPtrs));
	}

	private static native long call(
			long blockPtr,
			long instrPtr,
			long id,
			int idLen,
			long functionPtr,
			long[] argPtrs);

}
