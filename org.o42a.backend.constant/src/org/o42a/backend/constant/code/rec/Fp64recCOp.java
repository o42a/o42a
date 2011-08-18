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
import org.o42a.backend.constant.code.op.Fp64cOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.code.op.Fp64recOp;


public final class Fp64recCOp
		extends RecCOp<Fp64recOp, Fp64op>
		implements Fp64recOp {

	public Fp64recCOp(CCode<?> code, Fp64recOp underlying) {
		super(code, underlying);
	}

	@Override
	public final Fp64recCOp create(CCode<?> code, Fp64recOp underlying) {
		return new Fp64recCOp(code, underlying);
	}

	@Override
	protected final Fp64cOp loaded(CCode<?> code, Fp64op underlying) {
		return new Fp64cOp(code, underlying);
	}

}
