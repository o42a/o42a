/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public final class SignatureBuilder {

	private final Signature<?> signature;
	private final SignatureWriter<?> writer;

	SignatureBuilder(Signature<?> signature, SignatureWriter<?> writer) {
		this.signature = signature;
		this.writer = writer;
	}

	public final Return<Void> returnVoid() {
		this.writer.returnVoid();
		return ret(new Return.ReturnVoid(this.signature));
	}

	public final Return<Int8op> returnInt8() {
		this.writer.returnInt8();
		return ret(new Return.ReturnInt8(this.signature));
	}

	public final Return<Int16op> returnInt16() {
		this.writer.returnInt16();
		return ret(new Return.ReturnInt16(this.signature));
	}

	public final Return<Int32op> returnInt32() {
		this.writer.returnInt32();
		return ret(new Return.ReturnInt32(this.signature));
	}

	public final Return<Int64op> returnInt64() {
		this.writer.returnInt64();
		return ret(new Return.ReturnInt64(this.signature));
	}

	public final Return<Fp32op> returnFp32() {
		this.writer.returnFp32();
		return ret(new Return.ReturnFp32(this.signature));
	}

	public final Return<Fp64op> returnFp64() {
		this.writer.returnFp64();
		return ret(new Return.ReturnFp64(this.signature));
	}

	public final Return<BoolOp> returnBool() {
		this.writer.returnBool();
		return ret(new Return.ReturnBool(this.signature));
	}

	public final Return<AnyOp> returnAny() {
		this.writer.returnAny();
		return ret(new Return.ReturnAny(this.signature));
	}

	public final Return<DataOp> returnData() {
		this.writer.returnData();
		return ret(new Return.ReturnData(this.signature));
	}

	public final <S extends StructOp<S>> Return<S> returnPtr(Type<S> type) {
		this.writer.returnPtr(type);
		return ret(new Return.ReturnPtr<>(this.signature, type));
	}

	public final Arg<Int8op> addInt8(String name) {
		return arg(IntType.INT8.arg(this.signature, argIndex(), name));
	}

	public final Arg<Int16op> addInt16(String name) {
		return arg(IntType.INT16.arg(this.signature, argIndex(), name));
	}

	public final Arg<Int32op> addInt32(String name) {
		return arg(IntType.INT32.arg(this.signature, argIndex(), name));
	}

	public final Arg<Int64op> addInt64(String name) {
		return arg(IntType.INT64.arg(this.signature, argIndex(), name));
	}

	public final Arg<Fp32op> addFp32(String name) {
		return arg(FpType.FP32.arg(this.signature, argIndex(), name));
	}

	public final Arg<Fp64op> addFp64(String name) {
		return arg(FpType.FP64.arg(this.signature, argIndex(), name));
	}

	public final Arg<BoolOp> addBool(String name) {
		return arg(new Arg.BoolArg(this.signature, argIndex(), name));
	}

	public final Arg<RelOp> addRelPtr(String name) {
		return arg(ScalarType.REL_PTR.arg(this.signature, argIndex(), name));
	}

	public final Arg<AnyOp> addPtr(String name) {
		return arg(PtrType.ANY_PTR.arg(this.signature, argIndex(), name));
	}

	public final Arg<DataOp> addData(String name) {
		return arg(PtrType.DATA_PTR.arg(this.signature, argIndex(), name));
	}

	public final <S extends StructOp<S>> Arg<S> addPtr(
			String name,
			Type<S> type) {
		type.pointer(this.signature.getGenerator());
		return arg(new Arg.PtrArg<>(this.signature, argIndex(), name, type));
	}

	public final <F extends Fn<F>> Arg<F> addFuncPtr(
			String name,
			Signature<F> signature) {
		this.signature.getGenerator().getFunctions().allocate(signature);
		return arg(new Arg.FuncPtrArg<>(
				this.signature,
				argIndex(),
				name,
				signature));
	}

	@Override
	public String toString() {
		return this.signature.toString();
	}

	private <R extends Return<?>> R ret(R ret) {
		this.signature.setReturn(ret);
		return ret;
	}

	private final int argIndex() {
		return this.signature.getArgs().length;
	}

	private <A extends Arg<?>> A arg(A arg) {
		arg.write(this.writer);
		this.signature.addArg(arg);
		return arg;
	}

}
