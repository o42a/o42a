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
import org.o42a.backend.constant.code.op.Int8cOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.op.Int8op;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.codegen.data.Ptr;


public final class Int8recCOp
		extends IntRecCOp<Int8recOp, Int8op, Byte>
		implements Int8recOp {

	public Int8recCOp(OpBE<Int8recOp> backend, RecStore store) {
		super(backend, store);
	}

	public Int8recCOp(
			OpBE<Int8recOp> backend,
			RecStore store,
			Ptr<Int8recOp> constant) {
		super(backend, store, constant);
	}

	@Override
	public Int8recOp create(OpBE<Int8recOp> backend, Ptr<Int8recOp> constant) {
		return new Int8recCOp(backend, null, constant);
	}

	@Override
	protected Int8op loaded(OpBE<Int8op> backend, Byte constant) {
		return new Int8cOp(backend, constant);
	}

	@Override
	protected Int8op underlyingConstant(CCodePart<?> part, Byte constant) {
		return part.underlying().int8(constant);
	}

}
