/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.backend.constant.code.op.Fp32cOp;
import org.o42a.codegen.code.op.Fp32op;
import org.o42a.codegen.code.op.Fp32recOp;
import org.o42a.codegen.data.Ptr;


public final class Fp32recCOp
		extends RecCOp<Fp32recOp, Fp32op, Float>
		implements Fp32recOp {

	public Fp32recCOp(
			CCode<?> code,
			Fp32recOp underlying,
			Ptr<Fp32recOp> constant) {
		super(code, underlying, constant);
	}

	@Override
	public Fp32recCOp create(
			CCode<?> code,
			Fp32recOp underlying,
			Ptr<Fp32recOp> constant) {
		return new Fp32recCOp(code, underlying, constant);
	}

	@Override
	protected Fp32cOp loaded(CCode<?> code, Fp32op underlying, Float constant) {
		return new Fp32cOp(code, underlying, constant);
	}

	@Override
	protected Fp32op underlyingConstant(CCode<?> code, Float constant) {
		return code.fp32(constant);
	}

}
