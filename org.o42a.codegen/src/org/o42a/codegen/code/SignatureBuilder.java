/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


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

	public final Return<Int32op> returnInt32() {
		this.writer.returnInt32();
		return ret(new Return.ReturnInt32(this.signature));
	}

	public final Return<Int64op> returnInt64() {
		this.writer.returnInt64();
		return ret(new Return.ReturnInt64(this.signature));
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

	public final <O extends StructOp> Return<O> returnPtr(Type<O> type) {
		this.writer.returnPtr(type);
		return new Return.ReturnPtr<O>(this.signature, type);
	}

	public final Arg<Int32op> addInt32(String name) {
		this.writer.addInt32();
		return arg(new Arg.Int32arg(this.signature, argIndex(), name));
	}

	public final Arg<Int64op> addInt64(String name) {
		this.writer.addInt64();
		return arg(new Arg.Int64arg(this.signature, argIndex(), name));
	}

	public final Arg<Fp64op> addFp64(String name) {
		this.writer.addFp64();
		return arg(new Arg.Fp64arg(this.signature, argIndex(), name));
	}

	public final Arg<BoolOp> addBool(String name) {
		this.writer.addBool();
		return arg(new Arg.BoolArg(this.signature, argIndex(), name));
	}

	public final Arg<RelOp> addRelPtr(String name) {
		this.writer.addRelPtr();
		return arg(new Arg.RelPtrArg(this.signature, argIndex(), name));
	}

	public final Arg<AnyOp> addAny(String name) {
		this.writer.addAny();
		return arg(new Arg.AnyArg(this.signature, argIndex(), name));
	}

	public final Arg<DataOp> addData(String name) {
		this.writer.addData();
		return arg(new Arg.DataArg(this.signature, argIndex(), name));
	}

	public final <O extends StructOp> Arg<O> addPtr(
			String name,
			Type<O> type) {
		type.pointer(this.signature.getGenerator());
		this.writer.addPtr(type);
		return arg(new Arg.PtrArg<O>(this.signature, argIndex(), name, type));
	}

	public final <F extends Func> Arg<F> addFuncPtr(
			String name,
			Signature<F> signature) {
		this.signature.getGenerator().getFunctions().allocate(signature);
		this.writer.addFuncPtr(signature);
		return arg(new Arg.FuncPtrArg<F>(
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
		this.signature.addArg(arg);
		return arg;
	}

}
