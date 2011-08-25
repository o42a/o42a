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
import org.o42a.codegen.code.op.Int8op;


public final class Int8cOp extends IntCOp<Int8op, Byte> implements Int8op {

	public Int8cOp(CCode<?> code, Int8op underlying, Byte constant) {
		super(code, underlying, constant);
	}

	@Override
	public Int8cOp create(CCode<?> code, Int8op underlying, Byte constant) {
		return new Int8cOp(code, underlying, constant);
	}

	@Override
	protected Int8op underlyingConstant(CCode<?> code, Byte constant) {
		return code.getUnderlying().int8(constant);
	}

	@Override
	protected Byte neg(Byte value) {
		return (byte) -value;
	}

	@Override
	protected Byte add(Byte value1, Byte value2) {
		return (byte) (value1 + value2);
	}

	@Override
	protected Byte sub(Byte value1, Byte value2) {
		return (byte) (value1 - value2);
	}

	@Override
	protected Byte mul(Byte value1, Byte value2) {
		return (byte) (value1 * value2);
	}

	@Override
	protected Byte div(Byte value1, Byte value2) {
		return (byte) (value1 / value2);
	}

	@Override
	protected Byte rem(Byte value1, Byte value2) {
		return (byte) (value1 % value2);
	}

	@Override
	protected int cmp(Byte value1, Byte value2) {
		return value1.compareTo(value2);
	}

	@Override
	protected Byte shl(Byte value, int numBits) {
		return (byte) (value << numBits);
	}

	@Override
	protected Byte lshr(Byte value, int numBits) {
		return (byte) (value >>> numBits);
	}

	@Override
	protected Byte ashr(Byte value, int numBits) {
		return (byte) (value >> numBits);
	}

	@Override
	protected Byte and(Byte value1, Byte value2) {
		return (byte) (value1 & value2);
	}

	@Override
	protected Byte or(Byte value1, Byte value2) {
		return (byte) (value1 | value2);
	}

	@Override
	protected Byte xor(Byte value1, Byte value2) {
		return (byte) (value1 ^ value2);
	}

	@Override
	protected Byte comp(Byte value) {
		return (byte) ~value;
	}

}
