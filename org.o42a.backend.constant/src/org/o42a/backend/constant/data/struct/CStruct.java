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
package org.o42a.backend.constant.data.struct;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.DataCOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.op.PtrCOp;
import org.o42a.backend.constant.code.rec.*;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.rec.*;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public final class CStruct<S extends StructOp<S>>
		extends PtrCOp<S, Ptr<S>>
		implements StructWriter<S> {

	private final Type<S> type;

	public CStruct(OpBE<S> backend, AllocClass allocClass, Type<S> type) {
		this(backend, allocClass, type, null);
	}

	public CStruct(
			OpBE<S> backend,
			AllocClass allocClass,
			Type<S> type,
			Ptr<S> constant) {
		super(backend, allocClass, constant);
		this.type = type;
	}

	@Override
	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public AnyRecOp field(CodeId id, Code code, Data<?> field) {

		final CDAlloc<?, ?> alloc =
				(CDAlloc<?, ?>) field.getPointer().getAllocation();

		return new AnyRecCOp(
				new OpBE<AnyRecOp>(id, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyRecOp write() {
						return backend().underlying().writer().field(
								getId(),
								part().underlying(),
								alloc.getUnderlying());
					}
				},
				getAllocClass());
	}

	@Override
	public Int8recCOp int8(CodeId id, Code code, Int8rec field) {

		final Ptr<Int8recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Int8cdAlloc fld =
				(Int8cdAlloc) field.getPointer().getAllocation();

		return new Int8recCOp(
				new OpBE<Int8recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int8recOp write() {
						return backend().underlying().writer().int8(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public Int16recCOp int16(CodeId id, Code code, Int16rec field) {

		final Ptr<Int16recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Int16cdAlloc fld =
				(Int16cdAlloc) field.getPointer().getAllocation();

		return new Int16recCOp(
				new OpBE<Int16recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int16recOp write() {
						return backend().underlying().writer().int16(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public Int32recCOp int32(CodeId id, Code code, Int32rec field) {

		final Ptr<Int32recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Int32cdAlloc fld =
				(Int32cdAlloc) field.getPointer().getAllocation();

		return new Int32recCOp(
				new OpBE<Int32recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int32recOp write() {
						return backend().underlying().writer().int32(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public Int64recCOp int64(CodeId id, Code code, Int64rec field) {

		final Ptr<Int64recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Int64cdAlloc fld =
				(Int64cdAlloc) field.getPointer().getAllocation();

		return new Int64recCOp(
				new OpBE<Int64recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Int64recOp write() {
						return backend().underlying().writer().int64(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public Fp32recCOp fp32(CodeId id, Code code, Fp32rec field) {

		final Ptr<Fp32recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Fp32cdAlloc fld =
				(Fp32cdAlloc) field.getPointer().getAllocation();

		return new Fp32recCOp(
				new OpBE<Fp32recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Fp32recOp write() {
						return backend().underlying().writer().fp32(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public Fp64recCOp fp64(CodeId id, Code code, Fp64rec field) {

		final Ptr<Fp64recOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final Fp64cdAlloc fld =
				(Fp64cdAlloc) field.getPointer().getAllocation();

		return new Fp64recCOp(
				new OpBE<Fp64recOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected Fp64recOp write() {
						return backend().underlying().writer().fp64(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public AnyRecCOp ptr(CodeId id, Code code, AnyRec field) {

		final Ptr<AnyRecOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final AnyRecCDAlloc fld =
				(AnyRecCDAlloc) field.getPointer().getAllocation();

		return new AnyRecCOp(
				new OpBE<AnyRecOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyRecOp write() {
						return backend().underlying().writer().ptr(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public DataRecCOp ptr(CodeId id, Code code, DataRec field) {

		final Ptr<DataRecOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final DataRecCDAlloc fld =
				(DataRecCDAlloc) field.getPointer().getAllocation();

		return new DataRecCOp(
				new OpBE<DataRecOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected DataRecOp write() {
						return backend().underlying().writer().ptr(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public <SS extends StructOp<SS>> StructRecCOp<SS> ptr(
			CodeId id,
			Code code,
			StructRec<SS> field) {

		final Ptr<StructRecOp<SS>> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final StructRecCDAlloc<SS> fld =
				(StructRecCDAlloc<SS>) field.getPointer().getAllocation();

		return new StructRecCOp<SS>(
				new OpBE<StructRecOp<SS>>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected StructRecOp<SS> write() {
						return backend().underlying().writer().ptr(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				field.getType(),
				pointer);
	}

	@Override
	public RelRecCOp relPtr(CodeId id, Code code, RelRec field) {

		final Ptr<RelRecOp> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final RelRecCDAlloc fld =
				(RelRecCDAlloc) field.getPointer().getAllocation();

		return new RelRecCOp(
				new OpBE<RelRecOp>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected RelRecOp write() {
						return backend().underlying().writer().relPtr(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				pointer);
	}

	@Override
	public <SS extends StructOp<SS>> SS struct(
			CodeId id,
			Code code,
			Type<SS> field) {

		final Ptr<SS> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();
			final ContainerCDAlloc<SS> f =
					(ContainerCDAlloc<SS>) field.pointer(
							getBackend().getGenerator()).getAllocation();

			pointer = constAlloc.field(f.getData()).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final CType<SS> fld = getBackend().underlying(field);

		return field.op(new CStruct<SS>(
				new OpBE<SS>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected SS write() {
						return backend().underlying().writer().struct(
								getId(),
								part().underlying(),
								fld);
					}
				},
				getAllocClass(),
				field.getType(),
				pointer));
	}

	@Override
	public <F extends Func<F>> FuncCOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {

		final Ptr<FuncOp<F>> pointer;

		if (!isConstant()) {
			pointer = null;
		} else {

			final ContainerCDAlloc<S> constAlloc =
					(ContainerCDAlloc<S>) getConstant().getAllocation();

			pointer = constAlloc.field(field).getPointer();
		}

		final CCode<?> ccode = cast(code);
		final FuncRecCDAlloc<F> fld =
				(FuncRecCDAlloc<F>) field.getPointer().getAllocation();

		return new FuncCOp<F>(
				new OpBE<FuncOp<F>>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected FuncOp<F> write() {
						return backend().underlying().writer().func(
								getId(),
								part().underlying(),
								fld.getUnderlying());
					}
				},
				getAllocClass(),
				field.getSignature(),
				pointer);
	}

	@Override
	public DataCOp toData(CodeId id, Code code) {
		return new DataCOp(
				new OpBE<DataOp>(id, cast(code)) {
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
				getAllocClass());
	}

	@Override
	public <SS extends StructOp<SS>> SS to(
			final CodeId id,
			final Code code,
			final Type<SS> type) {

		final CCode<?> ccode = cast(code);

		return type.op(new CStruct<SS>(
				new OpBE<SS>(id, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected SS write() {
						return backend().underlying().to(
								getId(),
								part().underlying(),
								getBackend().underlying(type));
					}
				},
				getAllocClass(),
				type,
				null));
	}

	@Override
	public S create(OpBE<S> backend, Ptr<S> constant) {
		return getType().op(new CStruct<S>(backend, null, getType(), constant));
	}

}
