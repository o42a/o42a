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
import org.o42a.backend.constant.code.op.RelCOp;
import org.o42a.backend.constant.data.RelCDAlloc;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.RelPtr;


public final class RelRecCOp
		extends RecCOp<RelRecOp, RelOp, RelPtr>
		implements RelRecOp {

	public RelRecCOp(OpBE<RelRecOp> backend, RecStore store) {
		super(backend, store);
	}

	public RelRecCOp(
			OpBE<RelRecOp> backend,
			RecStore store,
			Ptr<RelRecOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public RelRecOp create(OpBE<RelRecOp> backend, Ptr<RelRecOp> constant) {
		return new RelRecCOp(backend, null, constant);
	}

	@Override
	protected RelOp loaded(OpBE<RelOp> backend, RelPtr constant) {
		return new RelCOp(backend, constant);
	}

	@Override
	protected RelOp underlyingConstant(CCodePart<?> part, RelPtr constant) {

		final RelCDAlloc alloc = (RelCDAlloc) constant.getAllocation();

		return alloc.getUnderlying().op(null, part.underlying());
	}

}
