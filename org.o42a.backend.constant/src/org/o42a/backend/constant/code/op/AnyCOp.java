/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.rec.*;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;


public final class AnyCOp extends PtrCOp<AnyOp, Ptr<AnyOp>> implements AnyOp {

	public AnyCOp(
			OpBE<AnyOp> backend,
			AllocClass allocClass,
			Ptr<AnyOp> constant) {
		super(backend, allocClass, constant);
	}

	public AnyCOp(OpBE<AnyOp> backend, AllocClass allocClass) {
		super(backend, allocClass);
	}

	@Override
	public final AnyRecCOp toPtr(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "any", this);

		return new AnyRecCOp(
				new OpBE<AnyRecOp>(castId, cast(code)) {
					@Override
					protected AnyRecOp write() {
						return backend().underlying().toPtr(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Int8recCOp toInt8(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "int8", this);

		return new Int8recCOp(
				new OpBE<Int8recOp>(castId, cast(code)) {
					@Override
					protected Int8recOp write() {
						return backend().underlying().toInt8(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Int16recCOp toInt16(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "int16", this);

		return new Int16recCOp(
				new OpBE<Int16recOp>(castId, cast(code)) {
					@Override
					protected Int16recOp write() {
						return backend().underlying().toInt16(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Int32recCOp toInt32(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "int32", this);

		return new Int32recCOp(
				new OpBE<Int32recOp>(castId, cast(code)) {
					@Override
					protected Int32recOp write() {
						return backend().underlying().toInt32(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Int64recCOp toInt64(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "int64", this);

		return new Int64recCOp(
				new OpBE<Int64recOp>(castId, cast(code)) {
					@Override
					protected Int64recOp write() {
						return backend().underlying().toInt64(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Fp32recCOp toFp32(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "fp32", this);

		return new Fp32recCOp(
				new OpBE<Fp32recOp>(castId, cast(code)) {
					@Override
					protected Fp32recOp write() {
						return backend().underlying().toFp32(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final Fp64recOp toFp64(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "fp64", this);

		return new Fp64recCOp(
				new OpBE<Fp64recOp>(castId, cast(code)) {
					@Override
					protected Fp64recOp write() {
						return backend().underlying().toFp64(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final RelRecCOp toRel(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "rel", this);

		return new RelRecCOp(
				new OpBE<RelRecOp>(castId, cast(code)) {
					@Override
					protected RelRecOp write() {
						return backend().underlying().toRel(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public DataCOp toData(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "data", this);

		return new DataCOp(
				new OpBE<DataOp>(castId, cast(code)) {
					@Override
					protected DataOp write() {
						return backend().underlying().toData(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

	@Override
	public final <F extends Func<F>> FuncCOp<F> toFunc(
			final CodeId id,
			final Code code,
			final Signature<F> signature) {

		final CodeId castId =
				code.getOpNames().castId(id, signature.getId(), this);

		return new FuncCOp<F>(
				new OpBE<FuncOp<F>>(castId, cast(code)) {
					@Override
					protected FuncOp<F> write() {
						return backend().underlying().toFunc(
								getId(),
								part().underlying(),
								getBackend().underlying(signature));
					}
				},
				getAllocClass(),
				signature);
	}

	@Override
	public <S extends StructOp<S>> S to(
			final CodeId id,
			final Code code,
			final Type<S> type) {

		final CodeId castId = code.getOpNames().castId(id, type.getId(), this);

		return type.op(new CStruct<S>(
				new OpBE<S>(castId, cast(code)) {
					@Override
					protected S write() {
						return backend().underlying().to(
								getId(),
								part().underlying(),
								getBackend().underlying(type));
					}
				},
				getAllocClass(),
				type));
	}

	@Override
	public AnyCOp create(OpBE<AnyOp> backend, Ptr<AnyOp> constant) {
		return new AnyCOp(backend, getAllocClass(), constant);
	}

}
