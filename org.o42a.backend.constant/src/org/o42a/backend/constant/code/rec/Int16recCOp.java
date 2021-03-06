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
import org.o42a.backend.constant.code.op.Int16cOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.op.Int16op;
import org.o42a.codegen.code.op.Int16recOp;
import org.o42a.codegen.data.Ptr;


public final class Int16recCOp
		extends IntRecCOp<Int16recOp, Int16op, Short>
		implements Int16recOp {

	public Int16recCOp(OpBE<Int16recOp> backend, RecStore store) {
		super(backend, store);
	}

	public Int16recCOp(
			OpBE<Int16recOp> backend,
			RecStore store,
			Ptr<Int16recOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public Int16recOp create(
			OpBE<Int16recOp> backend,
			Ptr<Int16recOp> constant) {
		return new Int16recCOp(backend, null, constant);
	}

	@Override
	protected Int16op loaded(OpBE<Int16op> backend, Short constant) {
		return new Int16cOp(backend, constant);
	}

	@Override
	protected Int16op underlyingConstant(CCodePart<?> part, Short constant) {
		return part.underlying().int16(constant);
	}

}
