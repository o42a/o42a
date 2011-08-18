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
import org.o42a.backend.constant.code.op.Int32cOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int32recOp;


public final class Int32recCOp
		extends RecCOp<Int32recOp, Int32op>
		implements Int32recOp {

	public Int32recCOp(CCode<?> code, Int32recOp underlying) {
		super(code, underlying);
	}

	@Override
	public final Int32recCOp create(CCode<?> code, Int32recOp underlying) {
		return new Int32recCOp(code, underlying);
	}

	@Override
	protected final Int32cOp loaded(CCode<?> code, Int32op underlying) {
		return new Int32cOp(code, underlying);
	}

}
