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
package org.o42a.backend.llvm.code;

import static org.o42a.backend.llvm.data.LLVMId.codeId;
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class LLVMFunction<F extends Func<F>>
		extends LLVMCode
		implements FuncWriter<F> {

	private final Function<F> function;
	private final CodeCallback callback;
	private final LLVMFuncAllocation<F> allocation;
	private long functionPtr;

	LLVMFunction(
			LLVMModule module,
			Function<F> function,
			CodeCallback callback) {
		super(module, null, function, function.getId());
		this.function = function;
		this.callback = callback;
		init();
		getBlockPtr();
		this.allocation = new LLVMFuncAllocation<F>(
				module,
				codeId(this),
				function.getSignature());
	}

	public final long getFunctionPtr() {
		return this.functionPtr;
	}

	public final CodeCallback getCallback() {
		return this.callback;
	}

	@Override
	public LLVMFuncAllocation<F> getAllocation() {
		return this.allocation;
	}

	@Override
	public Int8op int8arg(Code code, Arg<Int8op> arg) {
		return new LLVMInt8op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int16op int16arg(Code code, Arg<Int16op> arg) {
		return new LLVMInt16op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int32op int32arg(Code code, Arg<Int32op> arg) {
		return new LLVMInt32op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Int64op int64arg(Code code, Arg<Int64op> arg) {
		return new LLVMInt64op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Fp32op fp32arg(Code code, Arg<Fp32op> arg) {
		return new LLVMFp32op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public Fp64op fp64arg(Code code, Arg<Fp64op> arg) {
		return new LLVMFp64op(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public BoolOp boolArg(Code code, Arg<BoolOp> arg) {
		return new LLVMBoolOp(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public RelOp relPtrArg(Code code, Arg<RelOp> arg) {
		return new LLVMRelOp(
				argId(arg),
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public AnyOp ptrArg(Code code, Arg<AnyOp> arg) {
		return new LLVMAnyOp(
				argId(arg),
				AUTO_ALLOC_CLASS,
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public DataOp dataArg(Code code, Arg<DataOp> arg) {
		return new LLVMDataOp(
				argId(arg),
				AUTO_ALLOC_CLASS,
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex()));
	}

	@Override
	public <S extends StructOp<S>> S ptrArg(
			Code code,
			Arg<S> arg,
			Type<S> type) {
		return type.op(new LLVMStruct<S>(
				argId(arg),
				AUTO_ALLOC_CLASS,
				type,
				nextPtr(code),
				arg(getFunctionPtr(), arg.getIndex())));
	}

	@Override
	public <FF extends Func<FF>> FF funcPtrArg(
			Code code,
			Arg<FF> arg,
			Signature<FF> signature) {
		return signature.op(new LLVMFunc<FF>(
				argId(arg),
				signature,
				nextPtr(code),
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
		this.functionPtr = createFunction(
				getModule().getNativePtr(),
				getId().getId(),
				getModule().nativePtr(this.function.getSignature()),
				this.function.isExported());
		return createBlock(this.functionPtr, getId().getId());
	}

	static native long externFunction(
			long modulePtr,
			String name,
			long signaturePtr);

	private static native long createFunction(
			long modulePtr,
			String name,
			long funcTypePtr,
			boolean exported);

	private final CodeId argId(Arg<?> arg) {
		return arg.getId();
	}

	private static native long arg(long functionPtr, int index);

	private static native boolean validate(long functionPtr);

}
