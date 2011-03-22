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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;


public abstract class StructOp implements PtrOp {

	private final StructWriter writer;

	public StructOp(StructWriter writer) {
		this.writer = writer;
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

			final RecOp<?> fieldOp = getWriter().field(code, field);

			fieldOp.allocated(code, this);
		}
	}

	@Override
	public final void returnValue(Code code) {
		getWriter().returnValue(code);
	}

	@Override
	public final BoolOp isNull(Code code) {
		return getWriter().isNull(code);
	}

	@Override
	public BoolOp eq(Code code, PtrOp other) {
		return getWriter().eq(code, other);
	}

	@Override
	public final AnyOp toAny(Code code) {
		return getWriter().toAny(code);
	}

	public final DataOp toData(Code code) {
		return getWriter().toData(code);
	}

	public <O extends StructOp> O to(Code code, Type<O> type) {
		return getWriter().to(code, type);
	}

	protected final RecOp<?> field(Code code, Data<?> field) {
		return getWriter().field(code, field);
	}

	protected final RecOp<Int8op> int8(Code code, Int8rec field) {
		return getWriter().int8(code, field);
	}

	protected final RecOp<Int16op> int16(Code code, Int16rec field) {
		return getWriter().int16(code, field);
	}

	protected final RecOp<Int32op> int32(Code code, Int32rec field) {
		return getWriter().int32(code, field);
	}

	protected final RecOp<Int64op> int64(Code code, Int64rec field) {
		return getWriter().int64(code, field);
	}

	protected final RecOp<Fp32op> fp32(Code code, Fp32rec field) {
		return getWriter().fp32(code, field);
	}

	protected final RecOp<Fp64op> fp64(Code code, Fp64rec field) {
		return getWriter().fp64(code, field);
	}

	protected final RecOp<AnyOp> ptr(Code code, AnyPtrRec field) {
		return getWriter().ptr(code, field);
	}

	protected final RecOp<DataOp> ptr(Code code, DataRec field) {
		return getWriter().ptr(code, field);
	}

	protected final <P extends StructOp> RecOp<P> ptr(
			Code code,
			StructRec<P> field) {
		return getWriter().ptr(code, field);
	}

	protected final RecOp<RelOp> relPtr(Code code, RelPtrRec field) {
		return getWriter().relPtr(code, field);
	}

	protected final <O extends StructOp> O struct(Code code, Type<O> field) {
		return getWriter().struct(code, field);
	}

	protected final <F extends Func> FuncOp<F> func(
			Code code,
			FuncRec<F> field) {
		return getWriter().func(code, field);
	}

}
