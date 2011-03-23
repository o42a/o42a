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

import org.o42a.backend.llvm.code.op.*;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class LLVMFunction<F extends Func>
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
	public Int8op int8arg(Code code, int index) {
		return new LLVMInt8op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public Int16op int16arg(Code code, int index) {
		return new LLVMInt16op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public Int32op int32arg(Code code, int index) {
		return new LLVMInt32op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public Int64op int64arg(Code code, int index) {
		return new LLVMInt64op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public Fp32op fp32arg(Code code, int index) {
		return new LLVMFp32op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public Fp64op fp64arg(Code code, int index) {
		return new LLVMFp64op(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public BoolOp boolArg(Code code, int index) {
		return new LLVMBoolOp(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public RelOp relPtrArg(Code code, int index) {
		return new LLVMRelOp(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public AnyOp ptrArg(Code code, int index) {
		return new LLVMAnyOp(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public DataOp dataArg(Code code, int index) {
		return new LLVMDataOp(
				nextPtr(code),
				arg(getFunctionPtr(), index));
	}

	@Override
	public <O extends StructOp> O ptrArg(Code code, int index, Type<O> type) {
		return type.op(new LLVMStruct(
				type,
				nextPtr(code),
				arg(getFunctionPtr(), index)));
	}

	@Override
	public <FF extends Func> FF funcPtrArg(
			Code code,
			int index,
			Signature<FF> signature) {
		return signature.op(new LLVMFunc<FF>(
				signature,
				nextPtr(code),
				arg(getFunctionPtr(), index)));
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

	private static native long arg(long functionPtr, int index);

	private static native boolean validate(long functionPtr);

}
