/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public abstract class StructOp<S extends StructOp<S>>
		implements AllocPtrOp<S>, DumpablePtrOp<S> {

	private final StructWriter<S> writer;

	public StructOp(StructWriter<S> writer) {
		this.writer = writer;
	}

	@Override
	public final ID getId() {
		return this.writer.getId();
	}

	@Override
	public final AllocPlace getAllocPlace() {
		return writer().getAllocPlace();
	}

	public Type<S> getType() {
		return writer().getType();
	}

	public final StructWriter<S> writer() {
		return this.writer;
	}

	@Override
	public void allocated(Code code, StructOp<?> enclosing) {
		for (Data<?> field : getType().iterate(getType().getGenerator())) {

			final ID fieldId = fieldId(null, code, field);
			final AllocPtrOp<?> fieldPtr = field.fieldOf(fieldId, code, this);

			fieldPtr.allocated(code, this);
		}
	}

	@Override
	public final void returnValue(Block code, boolean dispose) {
		writer().returnValue(code, dispose);
	}

	@Override
	public final BoolOp isNull(ID id, Code code) {
		return writer().isNull(id, code);
	}

	@Override
	public final BoolOp eq(ID id, Code code, S other) {
		return writer().eq(id, code, other);
	}

	@Override
	public final BoolOp ne(ID id, Code code, S other) {
		return writer().ne(id, code, other);
	}

	@Override
	public final S offset(ID id, Code code, IntOp<?> index) {
		return writer().offset(id, code, index);
	}

	public final Int8recOp int8(ID id, Code code, Int8rec field) {
		return writer().int8(fieldId(id, code, field), code, field);
	}

	public final Int16recOp int16(ID id, Code code, Int16rec field) {
		return writer().int16(fieldId(id, code, field), code, field);
	}

	public final Int32recOp int32(ID id, Code code, Int32rec field) {
		return writer().int32(fieldId(id, code, field), code, field);
	}

	public final Int64recOp int64(
			ID id,
			Code code,
			Int64rec field) {
		return writer().int64(fieldId(id, code, field), code, field);
	}

	public final Fp32recOp fp32(ID id, Code code, Fp32rec field) {
		return writer().fp32(fieldId(id, code, field), code, field);
	}

	public final Fp64recOp fp64(ID id, Code code, Fp64rec field) {
		return writer().fp64(fieldId(id, code, field), code, field);
	}

	public final SystemOp system(ID id, Code code, SystemData field) {
		return writer().system(fieldId(id, code, field), code, field);
	}

	public final AnyRecOp ptr(ID id, Code code, AnyRec field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final DataRecOp ptr(ID id, Code code, DataRec field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final <SS extends StructOp<SS>> StructRecOp<SS> ptr(
			ID id,
			Code code,
			StructRec<SS> field) {
		return writer().ptr(fieldId(id, code, field), code, field);
	}

	public final RelRecOp relPtr(
			ID id,
			Code code,
			RelRec field) {
		return writer().relPtr(fieldId(id, code, field), code, field);
	}

	public final <SS extends StructOp<SS>> SS struct(
			ID id,
			Code code,
			Type<SS> field) {
		return writer().struct(
				fieldId(id, code, field.data(code.getGenerator()).getId()),
				code,
				field);
	}

	public final <F extends Fn<F>> FuncOp<F> func(
			ID id,
			Code code,
			FuncRec<F> field) {
		return writer().func(fieldId(id, code, field), code, field);
	}

	@Override
	public final AnyOp toAny(ID id, Code code) {
		return writer().toAny(id, code);
	}

	@Override
	public final DataOp toData(ID id, Code code) {
		return writer().toData(code.opId(id), code);
	}

	public final <SS extends StructOp<SS>> SS to(
			ID id,
			Code code,
			Type<SS> type) {
		return writer().to(code.opId(id), code, type);
	}

	@Override
	public String toString() {

		final ID id = getId();

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
	protected ID fieldId(Code code, ID local) {
		return getId().setLocal(local);
	}

	private final ID fieldId(ID id, Code code, Data<?> field) {
		if (id != null) {
			return code.opId(id);
		}
		return fieldId(code, field.getId());
	}

	private final ID fieldId(ID id, Code code, ID field) {
		if (id != null) {
			return code.opId(id);
		}
		return fieldId(code, field);
	}

}
