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
import org.o42a.backend.constant.code.op.AnyCOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.data.Ptr;


public final class AnyRecCOp
		extends AtomicRecCOp<AnyRecOp, AnyOp, Ptr<AnyOp>>
		implements AnyRecOp {

	public AnyRecCOp(OpBE<AnyRecOp> backend, RecStore store) {
		super(backend, store);
	}

	public AnyRecCOp(
			OpBE<AnyRecOp> backend,
			RecStore store,
			Ptr<AnyRecOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public AnyRecOp create(OpBE<AnyRecOp> backend, Ptr<AnyRecOp> constant) {
		return new AnyRecCOp(backend, null, constant);
	}

	@Override
	protected AnyCOp loaded(OpBE<AnyOp> backend, Ptr<AnyOp> constant) {
		return new AnyCOp(backend, null, constant);
	}

	@Override
	protected AnyOp underlyingConstant(CCodePart<?> part, Ptr<AnyOp> constant) {

		final AnyCDAlloc alloc = (AnyCDAlloc) constant.getAllocation();

		return alloc.getUnderlyingPtr().op(null, part.underlying());
	}

}
