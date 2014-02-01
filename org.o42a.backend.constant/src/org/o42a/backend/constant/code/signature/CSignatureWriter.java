/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code.signature;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.SignatureAllocation;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.ArrayUtil;


public class CSignatureWriter<F extends Func<F>>
		implements SignatureWriter<F> {

	private static final CArg<?>[] NO_ARGS = new CArg<?>[0];

	private final ConstBackend backend;
	private final Signature<F> orignalSignature;

	private CArgType ret;
	private CType<?> returnType;
	private CArg<?>[] args = NO_ARGS;

	public CSignatureWriter(
			ConstBackend backend,
			Signature<F> orignalSignature) {
		this.backend = backend;
		this.orignalSignature = orignalSignature;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	public final Signature<F> getOrignalSignature() {
		return this.orignalSignature;
	}

	@Override
	public void returnVoid() {
		ret(CArgType.VOID);
	}

	@Override
	public void returnInt8() {
		ret(CArgType.INT8);
	}

	@Override
	public void returnInt16() {
		ret(CArgType.INT16);
	}

	@Override
	public void returnInt32() {
		ret(CArgType.INT32);
	}

	@Override
	public void returnInt64() {
		ret(CArgType.INT64);
	}

	@Override
	public void returnFp32() {
		ret(CArgType.FP32);
	}

	@Override
	public void returnFp64() {
		ret(CArgType.FP64);
	}

	@Override
	public void returnBool() {
		ret(CArgType.BOOL);
	}

	@Override
	public void returnAny() {
		ret(CArgType.ANY);
	}

	@Override
	public void returnData() {
		ret(CArgType.DATA);
	}

	@Override
	public void returnPtr(Type<?> type) {
		this.returnType = getBackend().underlying(type);
	}

	@Override
	public void addInt8(Arg<Int8op> arg) {
		arg(new SimpleCArg<>(CArgType.INT8, arg));
	}

	@Override
	public void addInt16(Arg<Int16op> arg) {
		arg(new SimpleCArg<>(CArgType.INT16, arg));
	}

	@Override
	public void addInt32(Arg<Int32op> arg) {
		arg(new SimpleCArg<>(CArgType.INT32, arg));
	}

	@Override
	public void addInt64(Arg<Int64op> arg) {
		arg(new SimpleCArg<>(CArgType.INT64, arg));
	}

	@Override
	public void addFp32(Arg<Fp32op> arg) {
		arg(new SimpleCArg<>(CArgType.FP32, arg));
	}

	@Override
	public void addFp64(Arg<Fp64op> arg) {
		arg(new SimpleCArg<>(CArgType.FP64, arg));
	}

	@Override
	public void addBool(Arg<BoolOp> arg) {
		arg(new SimpleCArg<>(CArgType.BOOL, arg));
	}

	@Override
	public void addRelPtr(Arg<RelOp> arg) {
		arg(new SimpleCArg<>(CArgType.REL, arg));
	}

	@Override
	public void addPtr(Arg<AnyOp> arg) {
		arg(new SimpleCArg<>(CArgType.ANY, arg));
	}

	@Override
	public void addData(Arg<DataOp> arg) {
		arg(new SimpleCArg<>(CArgType.DATA, arg));
	}

	@Override
	public <S extends StructOp<S>> void addPtr(Arg<S> arg, Type<S> type) {
		arg(new PtrCArg<>(getBackend().underlying(type), arg));
	}

	@Override
	public <FF extends Func<FF>> void addFuncPtr(
			Arg<FF> arg,
			Signature<FF> signature) {
		arg(new FuncPtrCArg<>(getBackend().underlying(signature), arg));
	}

	@Override
	public SignatureAllocation<F> done() {
		return new CSignature<>(this);
	}

	final CArg<?>[] getArgs() {
		return this.args;
	}

	void rebuild(SignatureBuilder builder) {
		if (this.returnType != null) {
			builder.returnPtr(this.returnType);
		} else {
			this.ret.setReturn(builder);
		}
		for (CArg<?> arg : this.args) {
			arg.add(builder);
		}
	}

	private final void ret(CArgType argType) {
		this.ret = argType;
	}

	private final void arg(CArg<?> arg) {
		this.args = ArrayUtil.append(this.args, arg);
	}

}
