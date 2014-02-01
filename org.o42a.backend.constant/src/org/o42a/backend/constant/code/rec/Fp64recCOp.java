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
import org.o42a.backend.constant.code.op.Fp64cOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.code.op.Fp64recOp;
import org.o42a.codegen.data.Ptr;


public final class Fp64recCOp
		extends RecCOp<Fp64recOp, Fp64op, Double>
		implements Fp64recOp {

	public Fp64recCOp(OpBE<Fp64recOp> backend, RecStore store) {
		super(backend, store);
	}

	public Fp64recCOp(
			OpBE<Fp64recOp> backend,
			RecStore store,
			Ptr<Fp64recOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public Fp64recOp create(OpBE<Fp64recOp> backend, Ptr<Fp64recOp> constant) {
		return new Fp64recCOp(backend, null, constant);
	}

	@Override
	protected Fp64op loaded(OpBE<Fp64op> backend, Double constant) {
		return new Fp64cOp(backend, constant);
	}

	@Override
	protected Fp64op underlyingConstant(CCodePart<?> part, Double constant) {
		return part.underlying().fp64(constant);
	}

}
