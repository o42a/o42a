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
package org.o42a.backend.llvm.code;

import static org.o42a.backend.llvm.id.LLVMId.functionId;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.NativeBuffer;
import org.o42a.backend.llvm.data.alloc.LLFAlloc;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class LLFunction<F extends Func<F>>
		extends LLBlock
		implements FuncWriter<F> {

	private final Function<F> function;
	private final Disposal beforeReturn;
	private final LLFAlloc<F> allocation;
	private long functionPtr;

	LLFunction(
			LLVMModule module,
			Function<F> function,
			Disposal beforeReturn) {
		super(module, null, function);
		this.function = function;
		this.beforeReturn = beforeReturn;
		init();
		getBlockPtr();
		this.allocation = new LLFAlloc<>(
				module,
				functionId(this),
				function.getSignature());
	}

	public final long getFunctionPtr() {
		return this.functionPtr;
	}

	public final void beforeReturn(Block code) {
		this.beforeReturn.dispose(code);
	}

	@Override
	public LLFAlloc<F> getAllocation() {
		return this.allocation;
	}

	@Override
	public Int8op int8arg(Code code, Arg<Int8op> arg) {
		return new Int8llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int16op int16arg(Code code, Arg<Int16op> arg) {
		return new Int16llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int32op int32arg(Code code, Arg<Int32op> arg) {
		return new Int32llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int64op int64arg(Code code, Arg<Int64op> arg) {
		return new Int64llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Fp32op fp32arg(Code code, Arg<Fp32op> arg) {
		return new Fp32llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Fp64op fp64arg(Code code, Arg<Fp64op> arg) {
		return new Fp64llOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public BoolOp boolArg(Code code, Arg<BoolOp> arg) {
		return new BoolLLOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public RelOp relPtrArg(Code code, Arg<RelOp> arg) {
		return new RelLLOp(
				arg.getId(),
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public AnyOp ptrArg(Code code, Arg<AnyOp> arg) {
		return new AnyLLOp(
				arg.getId(),
				null,
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public DataOp dataArg(Code code, Arg<DataOp> arg) {
		return new DataLLOp(
				arg.getId(),
				null,
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public <S extends StructOp<S>> S ptrArg(
			Code code,
			Arg<S> arg,
			Type<S> type) {
		return type.op(new LLStruct<>(
				arg.getId(),
				null,
				type,
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex())));
	}

	@Override
	public <FF extends Func<FF>> FF funcPtrArg(
			Code code,
			Arg<FF> arg,
			Signature<FF> signature) {
		return signature.op(new LLFunc<>(
				arg.getId(),
				signature,
				llvm(code).nextPtr(),
				arg(getFunctionPtr(), arg.getIndex())));
	}

	@Override
	public void done() {
		if (!validate(getFunctionPtr())) {
			throw new IllegalStateException(
					"Invalid function generated: " + this);
		}
	}

	@Override
	public String toString() {
		return this.function.toString();
	}

	@Override
	protected long createFirtsBlock() {

		final NativeBuffer ids = getFunction().getModule().ids();

		this.functionPtr = createFunction(
				getModule().getNativePtr(),
				ids.write(getId()),
				ids.length(),
				getModule().nativePtr(this.function.getSignature()),
				this.function.isExported());
		return createBlock(this, getId());
	}

	static native long externFunction(
			long modulePtr,
			long id,
			int idLen,
			long signaturePtr);

	private static native long createFunction(
			long modulePtr,
			long id,
			int idLen,
			long funcTypePtr,
			boolean exported);

	private static native long arg(long functionPtr, int index);

	private static native boolean validate(long functionPtr);

}
