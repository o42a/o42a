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
import org.o42a.backend.constant.code.op.Fp32cOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.op.Fp32op;
import org.o42a.codegen.code.op.Fp32recOp;
import org.o42a.codegen.data.Ptr;


public final class Fp32recCOp
		extends RecCOp<Fp32recOp, Fp32op, Float>
		implements Fp32recOp {

	public Fp32recCOp(OpBE<Fp32recOp> backend, RecStore store) {
		super(backend, store);
	}

	public Fp32recCOp(
			OpBE<Fp32recOp> backend,
			RecStore store,
			Ptr<Fp32recOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public Fp32recOp create(OpBE<Fp32recOp> backend, Ptr<Fp32recOp> constant) {
		return new Fp32recCOp(backend, null, constant);
	}

	@Override
	protected Fp32op loaded(OpBE<Fp32op> backend, Float constant) {
		return new Fp32cOp(backend, constant);
	}

	@Override
	protected Fp32op underlyingConstant(CCodePart<?> part, Float constant) {
		return part.underlying().fp32(constant);
	}

}
