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

	public CStruct(CCode<?> code, S underlying, Type<S> type, Ptr<S> constant) {
		super(code, underlying, constant);
		this.type = type;
	}

	@Override
	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public AnyRecOp field(CodeId id, Code code, Data<?> field) {

		final CCode<?> ccode = cast(code);
		final CDAlloc<?, ?> alloc =
				(CDAlloc<?, ?>) field.getPointer().getAllocation();
		final AnyRecOp underlyingRec = getUnderlying().writer().field(
				id,
				ccode.getUnderlying(),
				alloc.getUnderlying());

		return new AnyRecCOp(ccode, underlyingRec, null);
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
		final Int8recOp underlyingRec = getUnderlying().writer().int8(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Int8recCOp(ccode, underlyingRec, pointer);
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
		final Int16recOp underlyingRec = getUnderlying().writer().int16(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Int16recCOp(ccode, underlyingRec, pointer);
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
		final Int32recOp underlyingRec = getUnderlying().writer().int32(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Int32recCOp(ccode, underlyingRec, pointer);
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
		final Int64recOp underlyingRec = getUnderlying().writer().int64(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Int64recCOp(ccode, underlyingRec, pointer);
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
		final Fp32recOp underlyingRec = getUnderlying().writer().fp32(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Fp32recCOp(ccode, underlyingRec, pointer);
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
		final Fp64recOp underlyingRec = getUnderlying().writer().fp64(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new Fp64recCOp(ccode, underlyingRec, pointer);
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
		final AnyRecOp underlyingRec = getUnderlying().writer().ptr(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new AnyRecCOp(ccode, underlyingRec, pointer);
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
		final DataRecOp underlyingRec = getUnderlying().writer().ptr(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new DataRecCOp(ccode, underlyingRec, pointer);
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
		final StructRecOp<SS> underlyingRec = getUnderlying().writer().ptr(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new StructRecCOp<SS>(
				ccode,
				underlyingRec,
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
		final RelRecOp underlyingRec = getUnderlying().writer().relPtr(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new RelRecCOp(ccode, underlyingRec, pointer);
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
		final SS underlyingStruct = getUnderlying().writer().struct(
				id,
				ccode.getUnderlying(),
				fld);

		return field.op(new CStruct<SS>(
				ccode,
				underlyingStruct,
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
		final FuncOp<F> underlyingRec = getUnderlying().writer().func(
				id,
				ccode.getUnderlying(),
				fld.getUnderlying());

		return new FuncCOp<F>(ccode, underlyingRec, pointer);
	}

	@Override
	public DataCOp toData(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final DataOp underlyingData =
				getUnderlying().toData(id, ccode.getUnderlying());

		return new DataCOp(ccode, underlyingData, null);
	}

	@Override
	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {

		final CCode<?> ccode = cast(code);
		final SS underlyingStruct = getUnderlying().to(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(type));

		return type.op(new CStruct<SS>(ccode, underlyingStruct, type, null));
	}

	@Override
	public S create(CCode<?> code, S underlying, Ptr<S> constantValue) {
		return getType().op(new CStruct<S>(
				code,
				underlying,
				getType(),
				constantValue));
	}

}
