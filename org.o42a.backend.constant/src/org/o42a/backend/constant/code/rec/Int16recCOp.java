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
import org.o42a.backend.constant.code.op.Int16cOp;
import org.o42a.codegen.code.op.Int16op;
import org.o42a.codegen.code.op.Int16recOp;


public final class Int16recCOp
		extends RecCOp<Int16recOp, Int16op>
		implements Int16recOp {

	public Int16recCOp(CCode<?> code, Int16recOp underlying) {
		super(code, underlying);
	}

	@Override
	public final Int16recCOp create(CCode<?> code, Int16recOp underlying) {
		return new Int16recCOp(code, underlying);
	}

	@Override
	protected final Int16cOp loaded(CCode<?> code, Int16op underlying) {
		return new Int16cOp(code, underlying);
	}

}
