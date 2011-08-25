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
import org.o42a.backend.constant.code.op.Int64cOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.data.Ptr;


public final class Int64recCOp
		extends RecCOp<Int64recOp, Int64op, Long>
		implements Int64recOp {

	public Int64recCOp(
			CCode<?> code,
			Int64recOp underlying,
			Ptr<Int64recOp> constant) {
		super(code, underlying, constant);
	}

	@Override
	public Int64recCOp create(
			CCode<?> code,
			Int64recOp underlying,
			Ptr<Int64recOp> constant) {
		return new Int64recCOp(code, underlying, constant);
	}

	@Override
	protected Int64cOp loaded(
			CCode<?> code,
			Int64op underlying,
			Long constant) {
		return new Int64cOp(code, underlying, constant);
	}

	@Override
	protected Int64op underlyingConstant(CCode<?> code, Long constant) {
		return code.getUnderlying().int64(constant);
	}

}
