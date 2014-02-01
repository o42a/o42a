/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.rec;

import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;


public class StructRecCOp<S extends StructOp<S>>
		extends AtomicRecCOp<StructRecOp<S>, S, Ptr<S>>
		implements StructRecOp<S> {

	private final Type<S> type;

	public StructRecCOp(
			OpBE<StructRecOp<S>> backend,
			RecStore store,
			Type<S> type) {
		super(backend, store);
		this.type = type;
	}

	public StructRecCOp(
			OpBE<StructRecOp<S>> backend,
			RecStore store,
			Type<S> type,
			Ptr<StructRecOp<S>> constant) {
		super(backend, store, constant);
		this.type = type;
	}

	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public StructRecOp<S> create(
			OpBE<StructRecOp<S>> backend,
			Ptr<StructRecOp<S>> constant) {
		return new StructRecCOp<>(backend, null, getType(), constant);
	}

	@Override
	protected S loaded(OpBE<S> backend, Ptr<S> constant) {
		return getType().op(new CStruct<>(backend, null, getType(), constant));
	}

	@Override
	protected S underlyingConstant(CCodePart<?> part, Ptr<S> constant) {

		final CDAlloc<S> alloc = (CDAlloc<S>) constant.getAllocation();

		return alloc.getUnderlyingPtr().op(null, part.underlying());
	}

}
