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
import org.o42a.codegen.code.op.Int32op;


public final class Int32cOp
		extends IntCOp<Int32op, Integer>
		implements Int32op {

	public Int32cOp(CCode<?> code, Int32op underlying, Integer constant) {
		super(code, underlying, constant);
	}

	@Override
	public Int32cOp create(
			CCode<?> code,
			Int32op underlying,
			Integer constant) {
		return new Int32cOp(code, underlying, constant);
	}

	@Override
	protected Int32op underlyingConstant(CCode<?> code, Integer constant) {
		return code.getUnderlying().int32(constant);
	}

	@Override
	protected Integer neg(Integer value) {
		return -value;
	}

	@Override
	protected Integer add(Integer value1, Integer value2) {
		return value1 + value2;
	}

	@Override
	protected Integer sub(Integer value1, Integer value2) {
		return value1 - value2;
	}

	@Override
	protected Integer mul(Integer value1, Integer value2) {
		return value1 * value2;
	}

	@Override
	protected Integer div(Integer value1, Integer value2) {
		return value1 / value2;
	}

	@Override
	protected Integer rem(Integer value1, Integer value2) {
		return value1 % value2;
	}

	@Override
	protected int cmp(Integer value1, Integer value2) {
		return value1.compareTo(value2);
	}

	@Override
	protected Integer shl(Integer value, int numBits) {
		return value << numBits;
	}

	@Override
	protected Integer lshr(Integer value, int numBits) {
		return value >>> numBits;
	}

	@Override
	protected Integer ashr(Integer value, int numBits) {
		return value >> numBits;
	}

	@Override
	protected Integer and(Integer value1, Integer value2) {
		return value1 & value2;
	}

	@Override
	protected Integer or(Integer value1, Integer value2) {
		return value1 | value2;
	}

	@Override
	protected Integer xor(Integer value1, Integer value2) {
		return value1 ^ value2;
	}

	@Override
	protected Integer comp(Integer value) {
		return ~value;
	}

}
