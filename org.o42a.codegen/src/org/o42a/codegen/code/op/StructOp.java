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
	public final BoolOp isNull(String name, Code code) {
		return getWriter().isNull(name, code);
	}

	@Override
	public BoolOp eq(String name, Code code, PtrOp other) {
		return getWriter().eq(name, code, other);
	}

	@Override
	public final AnyOp toAny(String name, Code code) {
		return getWriter().toAny(name, code);
	}

	public final DataOp toData(String name, Code code) {
		return getWriter().toData(code.nameId(name), code);
	}

	public <O extends StructOp> O to(String name, Code code, Type<O> type) {
		return getWriter().to(code.nameId(name), code, type);
	}

	protected final RecOp<?> field(String name, Code code, Data<?> field) {
		return getWriter().field(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Int8op> int8(String name, Code code, Int8rec field) {
		return getWriter().int8(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Int16op> int16(
			String name,
			Code code,
			Int16rec field) {
		return getWriter().int16(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Int32op> int32(
			String name,
			Code code,
			Int32rec field) {
		return getWriter().int32(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Int64op> int64(
			String name,
			Code code,
			Int64rec field) {
		return getWriter().int64(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Fp32op> fp32(String name, Code code, Fp32rec field) {
		return getWriter().fp32(fieldId(name, code, field), code, field);
	}

	protected final RecOp<Fp64op> fp64(String name, Code code, Fp64rec field) {
		return getWriter().fp64(fieldId(name, code, field), code, field);
	}

	protected final RecOp<AnyOp> ptr(String name, Code code, AnyPtrRec field) {
		return getWriter().ptr(fieldId(name, code, field), code, field);
	}

	protected final RecOp<DataOp> ptr(String name, Code code, DataRec field) {
		return getWriter().ptr(fieldId(name, code, field), code, field);
	}

	protected final <P extends StructOp> RecOp<P> ptr(
			String name,
			Code code,
			StructRec<P> field) {
		return getWriter().ptr(fieldId(name, code, field), code, field);
	}

	protected final RecOp<RelOp> relPtr(
			String name,
			Code code,
			RelPtrRec field) {
		return getWriter().relPtr(fieldId(name, code, field), code, field);
	}

	protected final <O extends StructOp> O struct(
			String name,
			Code code,
			Type<O> field) {
		return getWriter().struct(
				fieldId(name, code, field.getId()),
				code,
				field);
	}

	protected final <F extends Func> FuncOp<F> func(
			String name,
			Code code,
			FuncRec<F> field) {
		return getWriter().func(fieldId(name, code, field), code, field);
	}

	private final CodeId fieldId(String name, Code code, Data<?> field) {
		if (name != null) {
			return code.nameId(name);
		}
		return getId().setLocal(field.getId());
	}

	private final CodeId fieldId(String name, Code code, CodeId field) {
		if (name != null) {
			return code.nameId(name);
		}
		return getId().setLocal(field);
	}

}
