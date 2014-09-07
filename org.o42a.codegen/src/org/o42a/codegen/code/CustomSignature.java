/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public final class CustomSignature {

	private final ID id;
	private final ArrayList<Consumer<SignatureBuilder>> args;

	CustomSignature(ID id) {
		this.id = id;
		this.args = new ArrayList<>();
	}

	CustomSignature(ID id, int numArgs) {
		this.id = id;
		this.args = new ArrayList<>(numArgs);
	}

	public final ID getId() {
		return this.id;
	}

	public final CustomSignature addInt8(String name) {
		this.args.add(builder -> builder.addInt8(name));
		return this;
	}

	public final CustomSignature addInt16(String name) {
		this.args.add(builder -> builder.addInt16(name));
		return this;
	}

	public final CustomSignature addInt32(String name) {
		this.args.add(builder -> builder.addInt32(name));
		return this;
	}

	public final CustomSignature addInt64(String name) {
		this.args.add(builder -> builder.addInt64(name));
		return this;
	}

	public final CustomSignature addFp32(String name) {
		this.args.add(builder -> builder.addFp32(name));
		return this;
	}

	public final CustomSignature addFp64(String name) {
		this.args.add(builder -> builder.addFp64(name));
		return this;
	}

	public final CustomSignature addBool(String name) {
		this.args.add(builder -> builder.addBool(name));
		return this;
	}

	public final CustomSignature addRelPtr(String name) {
		this.args.add(builder -> builder.addRelPtr(name));
		return this;
	}

	public final CustomSignature addPtr(String name) {
		this.args.add(builder -> builder.addPtr(name));
		return this;
	}

	public final CustomSignature addData(String name) {
		this.args.add(builder -> builder.addData(name));
		return this;
	}

	public final <S extends StructOp<S>> CustomSignature addPtr(
			String name,
			Type<S> type) {
		this.args.add(builder -> builder.addPtr(name, type));
		return this;
	}

	public final <F extends Func<F>> CustomSignature addFuncPtr(
			String name,
			Signature<F> signature) {
		this.args.add(builder -> builder.addFuncPtr(name, signature));
		return this;
	}

	public final SimpleSignature<Void> returnVoid() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnVoid());
	}

	public final <F extends Func<F>> ExtSignature<Void, F> returnVoid(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnVoid(),
				createOp);
	}

	public final SimpleSignature<Int8op> returnInt8() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt8());
	}

	public final <F extends Func<F>> ExtSignature<Int8op, F> returnInt8(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt8(),
				createOp);
	}

	public final SimpleSignature<Int16op> returnInt16() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt16());
	}

	public final <F extends Func<F>> ExtSignature<Int16op, F> returnInt16(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt16(),
				createOp);
	}

	public final SimpleSignature<Int32op> returnInt32() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt32());
	}

	public final <F extends Func<F>> ExtSignature<Int32op, F> returnInt32(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt32(),
				createOp);
	}

	public final SimpleSignature<Int64op> returnInt64() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt64());
	}

	public final <F extends Func<F>> ExtSignature<Int64op, F> returnInt64(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnInt64(),
				createOp);
	}

	public final SimpleSignature<Fp32op> returnFp32() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnFp32());
	}

	public final <F extends Func<F>> ExtSignature<Fp32op, F> returnFp32(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnFp32(),
				createOp);
	}

	public final SimpleSignature<Fp64op> returnFp64() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnFp64());
	}

	public final <F extends Func<F>> ExtSignature<Fp64op, F> returnFp64(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnFp64(),
				createOp);
	}

	public final SimpleSignature<BoolOp> returnBool() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnBool());
	}

	public final <F extends Func<F>> ExtSignature<BoolOp, F> returnBool(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnBool(),
				createOp);
	}

	public final SimpleSignature<AnyOp> returnAny() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnAny());
	}

	public final <F extends Func<F>> ExtSignature<AnyOp, F> returnAny(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnAny(),
				createOp);
	}

	public final SimpleSignature<DataOp> returnData() {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnData());
	}

	public final <F extends Func<F>> ExtSignature<DataOp, F> returnData(
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnData(),
				createOp);
	}

	public final <S extends StructOp<S>> SimpleSignature<S> returnPtr(
			Type<S> type) {
		return new SimpleSignature<>(
				getId(),
				this.args,
				builder -> builder.returnPtr(type));
	}

	public final
	<F extends Func<F>, S extends StructOp<S>> ExtSignature<S, F> returnPtr(
			Type<S> type,
			Function<FuncCaller<F>, F> createOp) {
		return new ExtSignature<>(
				getId(),
				this.args,
				builder -> builder.returnPtr(type),
				createOp);
	}

}
