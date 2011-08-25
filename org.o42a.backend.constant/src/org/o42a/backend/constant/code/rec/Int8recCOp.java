/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.Int8cOp;
import org.o42a.codegen.code.op.Int8op;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.codegen.data.Ptr;


public final class Int8recCOp
		extends RecCOp<Int8recOp, Int8op, Byte>
		implements Int8recOp {

	public Int8recCOp(
			CCode<?> code,
			Int8recOp underlying,
			Ptr<Int8recOp> constant) {
		super(code, underlying, constant);
	}

	@Override
	public Int8recCOp create(
			CCode<?> code,
			Int8recOp underlying,
			Ptr<Int8recOp> constant) {
		return new Int8recCOp(code, underlying, constant);
	}

	@Override
	protected Int8cOp loaded(CCode<?> code, Int8op underlying, Byte constant) {
		return new Int8cOp(code, underlying, constant);
	}

	@Override
	protected Int8op underlyingConstant(CCode<?> code, Byte constant) {
		return code.getUnderlying().int8(constant);
	}

}
