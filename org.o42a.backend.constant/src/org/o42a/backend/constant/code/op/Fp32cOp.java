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
package org.o42a.backend.constant.code.op;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.Fp32op;


public final class Fp32cOp extends FpCOp<Fp32op, Float> implements Fp32op {

	public Fp32cOp(CCode<?> code, Fp32op underlying, Float constant) {
		super(code, underlying, constant);
	}

	@Override
	public Fp32cOp create(CCode<?> code, Fp32op underlying, Float constant) {
		return new Fp32cOp(code, underlying, constant);
	}

	@Override
	protected Fp32op underlyingConstant(CCode<?> code, Float constant) {
		return code.getUnderlying().fp32(constant);
	}

	@Override
	protected Float neg(Float value) {
		return -value;
	}

	@Override
	protected Float add(Float value1, Float value2) {
		return value1 + value2;
	}

	@Override
	protected Float sub(Float value1, Float value2) {
		return value1 - value2;
	}

	@Override
	protected Float mul(Float value1, Float value2) {
		return value1 * value2;
	}

	@Override
	protected Float div(Float value1, Float value2) {
		return value1 / value2;
	}

	@Override
	protected Float rem(Float value1, Float value2) {
		return value1 % value2;
	}

	@Override
	protected int cmp(Float value1, Float value2) {
		return value1.compareTo(value2);
	}

}
