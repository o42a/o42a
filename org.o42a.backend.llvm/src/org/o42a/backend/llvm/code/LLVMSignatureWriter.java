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

import static java.lang.System.arraycopy;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.data.Type;


final class LLVMSignatureWriter<F extends Func> implements SignatureWriter<F> {

	private final Signature<F> signature;
	private final LLVMModule module;

	private long returnType;
	private long[] params = new long[0];

	LLVMSignatureWriter(LLVMModule module, Signature<F> signature) {
		this.module = module;
		this.signature = signature;
	}

	@Override
	public void returnVoid() {
		this.returnType = this.module.voidType();
	}

	@Override
	public void returnInt8() {
		this.returnType = this.module.int8type();
	}

	@Override
	public void returnInt16() {
		this.returnType = this.module.int16type();
	}

	@Override
	public void returnInt32() {
		this.returnType = this.module.int32type();
	}

	@Override
	public void returnInt64() {
		this.returnType = this.module.int64type();
	}

	@Override
	public void returnFp32() {
		this.returnType = this.module.fp32type();
	}

	@Override
	public void returnFp64() {
		this.returnType = this.module.fp64type();
	}

	@Override
	public void returnBool() {
		this.returnType = this.module.boolType();
	}

	@Override
	public void returnAny() {
		this.returnType = this.module.anyType();
	}

	@Override
	public void returnData() {
		this.returnType = this.module.anyType();
	}

	@Override
	public void returnPtr(Type<?> type) {
		this.returnType = this.module.pointerTo(type);
	}

	@Override
	public void addInt8() {
		addParam(this.module.int8type());
	}

	@Override
	public void addInt16() {
		addParam(this.module.int16type());
	}

	@Override
	public void addInt32() {
		addParam(this.module.int32type());
	}

	@Override
	public void addInt64() {
		addParam(this.module.int64type());
	}

	@Override
	public void addFp32() {
		addParam(this.module.fp32type());
	}

	@Override
	public void addFp64() {
		addParam(this.module.fp64type());
	}

	@Override
	public void addBool() {
		addParam(this.module.boolType());
	}

	@Override
	public void addRelPtr() {
		addParam(this.module.relPtrType());
	}

	@Override
	public void addPtr() {
		addParam(this.module.anyType());
	}

	@Override
	public void addData() {
		addParam(this.module.anyType());
	}

	@Override
	public void addPtr(Type<?> type) {
		addParam(this.module.pointerTo(type));
	}

	@Override
	public void addFuncPtr(Signature<?> signature) {
		addParam(this.module.pointerTo(signature));
	}

	@Override
	public LLVMSignature<F> done() {

		final long signaturePtr = createSignature(
				this.module.getNativePtr(),
				this.signature.codeId(this.module.getGenerator()).toString(),
				this.returnType,
				this.params);

		return new LLVMSignature<F>(this.signature, signaturePtr);
	}

	private void addParam(long param) {

		final int len = this.params.length;

		if (len == 0) {
			this.params = new long[] {param};
			return;
		}

		final long[] params = new long[len + 1];

		arraycopy(this.params, 0, params, 0, len);
		params[len] = param;
		this.params = params;
	}

	private static native long createSignature(
			long modulePtr,
			String name,
			long returnTypePtr,
			long[] paramPtrs);

}
