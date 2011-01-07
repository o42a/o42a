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
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Type;


public abstract class StructOp implements PtrOp {

	private final StructWriter writer;

	public StructOp(StructWriter writer) {
		this.writer = writer;
	}

	public Type<?> getType() {
		return writer().getType();
	}

	@Override
	public final void returnValue(Code code) {
		writer().returnValue(code);
	}

	@Override
	public final BoolOp isNull(Code code) {
		return writer().isNull(code);
	}

	@Override
	public BoolOp eq(Code code, PtrOp other) {
		return writer().eq(code, other);
	}

	@Override
	public final AnyOp toAny(Code code) {
		return writer().toAny(code);
	}

	@Override
	public DataOp<AnyOp> toPtr(Code code) {
		return writer().toPtr(code);
	}

	@Override
	public final DataOp<Int32op> toInt32(Code code) {
		return writer().toInt32(code);
	}

	@Override
	public final DataOp<Int64op> toInt64(Code code) {
		return writer().toInt64(code);
	}

	@Override
	public final DataOp<Fp64op> toFp64(Code code) {
		return writer().toFp64(code);
	}

	@Override
	public final DataOp<RelOp> toRel(Code code) {
		return writer().toRel(code);
	}

	@Override
	public final <F extends Func> CodeOp<F> toFunc(
			Code code,
			Signature<F> signature) {

		final SignatureOpBase<F> sign = signature;

		return writer().toFunc(code, sign.allocate(writer().backend()));
	}

	@Override
	public final <O extends PtrOp> O to(Code code, Type<O> type) {
		return writer().to(code, type);
	}

	public abstract StructOp create(StructWriter writer);

	public final StructWriter writer() {
		return this.writer;
	}

}
