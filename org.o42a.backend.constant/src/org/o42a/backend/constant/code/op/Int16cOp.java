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
package org.o42a.backend.constant.code.op;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.Int16op;


public final class Int16cOp extends IntCOp<Int16op, Short> implements Int16op {

	public Int16cOp(CCode<?> code, Int16op underlying, Short constant) {
		super(code, underlying, constant);
	}

	@Override
	public Int16cOp create(CCode<?> code, Int16op underlying, Short constant) {
		return new Int16cOp(code, underlying, constant);
	}

	@Override
	protected Int16op underlyingConstant(CCode<?> code, Short constant) {
		return code.getUnderlying().int16(constant);
	}

	@Override
	protected Short neg(Short value) {
		return (short) -value;
	}

	@Override
	protected Short add(Short value1, Short value2) {
		return (short) (value1 + value2);
	}

	@Override
	protected Short sub(Short value1, Short value2) {
		return (short) (value1 - value2);
	}

	@Override
	protected Short mul(Short value1, Short value2) {
		return (short) (value1 * value2);
	}

	@Override
	protected Short div(Short value1, Short value2) {
		return (short) (value1 / value2);
	}

	@Override
	protected Short rem(Short value1, Short value2) {
		return (short) (value1 % value2);
	}

	@Override
	protected int cmp(Short value1, Short value2) {
		return value1.compareTo(value2);
	}

	@Override
	protected Short shl(Short value, int numBits) {
		return (short) (value << numBits);
	}

	@Override
	protected Short lshr(Short value, int numBits) {
		return (short) (value >>> numBits);
	}

	@Override
	protected Short ashr(Short value, int numBits) {
		return (short) (value >> numBits);
	}

	@Override
	protected Short and(Short value1, Short value2) {
		return (short) (value1 & value2);
	}

	@Override
	protected Short or(Short value1, Short value2) {
		return (short) (value1 | value2);
	}

	@Override
	protected Short xor(Short value1, Short value2) {
		return (short) (value1 ^ value2);
	}

	@Override
	protected Short comp(Short value) {
		return (short) ~value;
	}

}
