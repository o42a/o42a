/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;


public abstract class StructOp<S extends StructOp<S>> implements DataPtrOp<S> {

	private final StructWriter<S> writer;

	public StructOp(StructWriter<S> writer) {
		this.writer = writer;
	}

	@Override
	public final CodeId getId() {
		return this.writer.getId();
	}

	@Override
	public final AllocClass getAllocClass() {
		return writer().getAllocClass();
	}

	public Type<S> getType() {
		return writer().getType();
	}

	public final StructWriter<S> writer() {
		return this.writer;
	}

	@Override
	public void allocated(AllocationCode code, StructOp<?> enclosing) {
		for (Data<?> field : getType().iterate(getType().getGenerator())) {

			final CodeId fieldId = fieldId(null, code, field);
			final PtrOp<?> fieldPtr = field.fieldOf(fieldId, code, this);

			fieldPtr.allocated(code, this);
		}
	}

	@Override
	public void allocated(Allocator allocator, StructOp<?> enclosing) {
		for (Data<?> field : getType().iterate(getType().getGenerator())) {

			final CodeId fieldId = fieldId(null, allocator, field);
			final PtrOp<?> fieldPtr = field.fieldOf(fieldId, allocator, this);

			fieldPtr.allocated(allocator, this);
		}
	}

	@Override
	public final void returnValue(Block code) {
		writer().returnValue(code);
	}

	@Override
	public final BoolOp isNull(CodeId id, Code code) {
		return writer().isNull(id, code);
	}

	@Override
	public BoolOp eq(CodeId id, Code code, S other) {
		return writer().eq(id, code, other);
	}

	@Override
	public final S offset(CodeId id, Code code, IntOp<?> index) {
		return writer().offset(id, code, index);
	}

	public final Int8recOp int8(CodeId id, Code code, Int8rec field) {
		return writer().int8(fieldId(id, code, field), code, field);
	}

	public final Int16recOp int16(
			CodeId id,
			Code code,
			Int16rec field) {
		return writer().int16(fieldId(id, code, field), code, field);
	}

	public final Int32recOp int32(
			CodeId id,
			Code code,
			Int32rec field) {
		return writer().int32(fieldId(id, code, field), code, field);
	}

	public final Int64recOp int64(
			CodeId id,
			Code code,
			Int64rec field) {
		return writer().int64(fieldId(id, code, field), code, field);
	}

	public final Fp32recOp fp32(CodeId id, Code code, Fp32rec field) {
		return writer().fp32(fieldId(id, code, field), code, field);
	}

	public final Fp64recOp fp64(CodeId id, Code code, Fp64rec field) {
		return writer().fp64(fieldId(id, code, field), code, field);
	}

	public final AnyRecOp ptr(CodeId id, Code code, AnyRec field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final DataRecOp ptr(CodeId id, Code code, DataRec field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final <SS extends StructOp<SS>> StructRecOp<SS> ptr(
			CodeId id,
			Code code,
			StructRec<SS> field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final RelRecOp relPtr(
			CodeId id,
			Code code,
			RelRec field) {
		return writer().relPtr(fieldId(id, code, field), code, field);
	}

	public final <SS extends StructOp<SS>> SS struct(
			CodeId id,
			Code code,
			Type<SS> field) {
		return writer().struct(
				fieldId(id, code, field.data(code.getGenerator()).getId()),
				code,
				field);
	}

	public final <F extends Func<F>> FuncOp<F> func(
			CodeId id,
			Code code,
			FuncRec<F> field) {
		return writer().func(fieldId(id, code, field), code, field);
	}

	@Override
	public final AnyOp toAny(CodeId id, Code code) {
		return writer().toAny(id, code);
	}

	@Override
	public final DataOp toData(CodeId id, Code code) {
		return writer().toData(code.opId(id), code);
	}

	public <SS extends StructOp<SS>> SS to(
			CodeId id,
			Code code,
			Type<SS> type) {
		return writer().to(code.opId(id), code, type);
	}

	@Override
	public String toString() {

		final CodeId id = getId();

		if (id == null) {
			return super.toString();
		}

		return id.toString();
	}

	/**
	 * Builds an identifier of field access operation.
	 *
	 * @param code code.
	 * @param local local part of identifier.
	 *
	 * @return full identifier.
	 */
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
