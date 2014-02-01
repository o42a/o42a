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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.code.rec.RecStore.allocRecStore;
import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.struct.StructStore.allocStructStore;

import org.o42a.backend.constant.code.rec.*;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public final class AnyCOp extends DataPtrCOp<AnyOp> implements AnyOp {

	public AnyCOp(
			OpBE<AnyOp> backend,
			AllocPlace allocPlace,
			Ptr<AnyOp> constant) {
		super(backend, allocPlace, constant);
	}

	public AnyCOp(OpBE<AnyOp> backend, AllocPlace allocPlace) {
		super(backend, allocPlace);
	}

	@Override
	public final AnyRecCOp toRec(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, ANY_ID, this);

		return new AnyRecCOp(
				new OpBE<AnyRecOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyRecOp write() {
						return backend().underlying().toRec(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public DataRecOp toDataRec(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, DATA_ID, this);

		return new DataRecCOp(
				new OpBE<DataRecOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected DataRecOp write() {
						return backend().underlying().toDataRec(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Int8recCOp toInt8(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, INT8_ID, this);

		return new Int8recCOp(
				new OpBE<Int8recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int8recOp write() {
						return backend().underlying().toInt8(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Int16recCOp toInt16(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, INT16_ID, this);

		return new Int16recCOp(
				new OpBE<Int16recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int16recOp write() {
						return backend().underlying().toInt16(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Int32recCOp toInt32(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, INT32_ID, this);

		return new Int32recCOp(
				new OpBE<Int32recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int32recOp write() {
						return backend().underlying().toInt32(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Int64recCOp toInt64(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, INT64_ID, this);

		return new Int64recCOp(
				new OpBE<Int64recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int64recOp write() {
						return backend().underlying().toInt64(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Fp32recCOp toFp32(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, FP32_ID, this);

		return new Fp32recCOp(
				new OpBE<Fp32recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Fp32recOp write() {
						return backend().underlying().toFp32(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final Fp64recOp toFp64(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, FP64_ID, this);

		return new Fp64recCOp(
				new OpBE<Fp64recOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Fp64recOp write() {
						return backend().underlying().toFp64(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public final RelRecCOp toRel(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, REL_ID, this);

		return new RelRecCOp(
				new OpBE<RelRecOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected RelRecOp write() {
						return backend().underlying().toRel(
								getId(),
								part().underlying());
					}
				},
				allocRecStore(getAllocPlace()));
	}

	@Override
	public DataCOp toData(ID id, Code code) {

		final ID castId = code.getOpNames().castId(id, DATA_ID, this);

		return new DataCOp(
				new OpBE<DataOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected DataOp write() {
						return backend().underlying().toData(
								getId(),
								part().underlying());
					}
				},
				getAllocPlace());
	}

	@Override
	public <S extends StructOp<S>> S to(
			final ID id,
			final Code code,
			final Type<S> type) {

		final ID castId = code.getOpNames().castId(id, type.getId(), this);

		return type.op(new CStruct<>(
				new OpBE<S>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected S write() {
						return backend().underlying().to(
								getId(),
								part().underlying(),
								getBackend().underlying(type));
					}
				},
				allocStructStore(getAllocPlace()),
				type));
	}

	@Override
	public AnyCOp create(OpBE<AnyOp> backend, Ptr<AnyOp> constant) {
		return new AnyCOp(backend, getAllocPlace(), constant);
	}

}
