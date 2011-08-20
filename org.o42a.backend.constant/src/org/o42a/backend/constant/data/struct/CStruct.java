/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data.struct;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.PtrCOp;
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.RecCDAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public class CStruct<S extends StructOp<S>>
		extends PtrCOp<S>
		implements StructWriter<S> {

	public CStruct(CCode<?> code, S underlying) {
		super(code, underlying);
	}

	@Override
	public final Type<S> getType() {

		final CType<S> underlyingType =
				getBackend().underlying(getUnderlying().getType());

		return underlyingType.getOriginal();
	}

	@Override
	public AnyRecOp field(CodeId id, Code code, Data<?> field) {

		final CCode<?> ccode = ConstBackend.cast(code);
		final RecCDAlloc<?, ?, ?> alloc =
				(RecCDAlloc<?, ?, ?>) field.getPointer().getAllocation();
		final AnyRecOp underlyingRec = getUnderlying().writer().field(
				id,
				ccode.getUnderlying(),
				alloc.getUnderlying());

		return new AnyRecCOp(ccode, underlyingRec);
	}

	@Override
	public Int8recOp int8(CodeId id, Code code, Int8rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int16recOp int16(CodeId id, Code code, Int16rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int32recOp int32(CodeId id, Code code, Int32rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int64recOp int64(CodeId id, Code code, Int64rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp32recOp fp32(CodeId id, Code code, Fp32rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp64recOp fp64(CodeId id, Code code, Fp64rec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnyRecOp ptr(CodeId id, Code code, AnyPtrRec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRecOp ptr(CodeId id, Code code, DataRec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <SS extends StructOp<SS>> StructRecOp<SS> ptr(
			CodeId id,
			Code code,
			StructRec<SS> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelRecOp relPtr(CodeId id, Code code, RelRec field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <SS extends StructOp<SS>> SS struct(
			CodeId id,
			Code code,
			Type<SS> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F extends Func<F>> FuncOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataOp toData(CodeId id, Code code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public S create(CCode<?> code, S underlying) {
		return getType().op(new CStruct<S>(code, underlying));
	}

}
