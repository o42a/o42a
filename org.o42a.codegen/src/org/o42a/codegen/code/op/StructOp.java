/*
    Compiler Code Generator
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
package org.o42a.codegen.code.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;


public abstract class StructOp implements PtrOp {

	private final StructWriter writer;

	public StructOp(StructWriter writer) {
		this.writer = writer;
	}

	@Override
	public final CodeId getId() {
		return this.writer.getId();
	}

	@Override
	public final AllocClass getAllocClass() {
		return getWriter().getAllocClass();
	}

	public Type<?> getType() {
		return getWriter().getType();
	}

	public final StructWriter getWriter() {
		return this.writer;
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
		for (Data<?> field : getType().iterate(getType().getGenerator())) {

			final RecOp<?> fieldOp =
				getWriter().field(getId().sub(field.getId()), code, field);

			fieldOp.allocated(code, this);
		}
	}

	@Override
	public final void returnValue(Code code) {
		getWriter().returnValue(code);
	}

	@Override
	public final BoolOp isNull(CodeId id, Code code) {
		return getWriter().isNull(id, code);
	}

	@Override
	public BoolOp eq(CodeId id, Code code, PtrOp other) {
		return getWriter().eq(id, code, other);
	}

	@Override
	public final AnyOp toAny(CodeId id, Code code) {
		return getWriter().toAny(id, code);
	}

	public final DataOp toData(CodeId id, Code code) {
		return getWriter().toData(code.opId(id), code);
	}

	public <O extends StructOp> O to(CodeId id, Code code, Type<O> type) {
		return getWriter().to(code.opId(id), code, type);
	}

	protected final RecOp<?> field(CodeId id, Code code, Data<?> field) {
		return getWriter().field(fieldId(id, code, field), code, field);
	}

	protected final Int8recOp int8(CodeId id, Code code, Int8rec field) {
		return getWriter().int8(fieldId(id, code, field), code, field);
	}

	protected final Int16recOp int16(
			CodeId id,
			Code code,
			Int16rec field) {
		return getWriter().int16(fieldId(id, code, field), code, field);
	}

	protected final Int32recOp int32(
			CodeId id,
			Code code,
			Int32rec field) {
		return getWriter().int32(fieldId(id, code, field), code, field);
	}

	protected final Int64recOp int64(
			CodeId id,
			Code code,
			Int64rec field) {
		return getWriter().int64(fieldId(id, code, field), code, field);
	}

	protected final Fp32recOp fp32(CodeId id, Code code, Fp32rec field) {
		return getWriter().fp32(fieldId(id, code, field), code, field);
	}

	protected final Fp64recOp fp64(CodeId id, Code code, Fp64rec field) {
		return getWriter().fp64(fieldId(id, code, field), code, field);
	}

	protected final AnyRecOp ptr(CodeId id, Code code, AnyPtrRec field) {
		return getWriter().ptr(fieldId(id, code, field), code, field);
	}

	protected final DataRecOp ptr(CodeId id, Code code, DataRec field) {
		return getWriter().ptr(fieldId(id, code, field), code, field);
	}

	protected final <S extends StructOp> StructRecOp<S> ptr(
			CodeId id,
			Code code,
			StructRec<S> field) {
		return getWriter().ptr(fieldId(id, code, field), code, field);
	}

	protected final RelRecOp relPtr(
			CodeId id,
			Code code,
			RelPtrRec field) {
		return getWriter().relPtr(fieldId(id, code, field), code, field);
	}

	protected final <S extends StructOp> S struct(
			CodeId id,
			Code code,
			Type<S> field) {
		return getWriter().struct(
				fieldId(id, code, field.getId()),
				code,
				field);
	}

	protected final <F extends Func> FuncOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {
		return getWriter().func(fieldId(id, code, field), code, field);
	}

	protected CodeId fieldId(Code code, CodeId local) {
		return getId().setLocal(local);
	}

	private final CodeId fieldId(CodeId id, Code code, Data<?> field) {
		if (id != null) {
			return code.opId(id);
		}
		return fieldId(code, field.getId());
	}

	private final CodeId fieldId(CodeId id, Code code, CodeId field) {
		if (id != null) {
			return code.opId(id);
		}
		return fieldId(code, field);
	}

}
