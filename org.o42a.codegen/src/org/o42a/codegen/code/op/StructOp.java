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
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Type;


public abstract class StructOp implements DataOp {

	private final StructWriter writer;

	public StructOp(StructWriter writer) {
		this.writer = writer;
	}

	@Override
	public Type<?> getType() {
		return writer().getType();
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
		for (Data<?> field : getType().iterate(getType().getGenerator())) {

			final RecOp<?> fieldOp = writer().field(code, field);

			fieldOp.allocated(code, this);
		}
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
	public <O extends StructOp> O to(Code code, Type<O> type) {
		return writer().to(code, type);
	}

	public final StructWriter writer() {
		return this.writer;
	}

}
