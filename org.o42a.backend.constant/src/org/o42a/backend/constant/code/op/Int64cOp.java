/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.util.string.ID;


public final class Int64cOp extends IntCOp<Int64op, Long> implements Int64op {

	public Int64cOp(OpBE<Int64op> backend) {
		super(backend);
	}

	public Int64cOp(ID id, CCode<?> code, Long constant) {
		super(id, code, constant);
	}

	public Int64cOp(OpBE<Int64op> backend, Long constant) {
		super(backend, constant);
	}

	@Override
	public Int64cOp create(OpBE<Int64op> backend, Long constant) {
		return new Int64cOp(backend, constant);
	}

	@Override
	protected Int64op underlyingConstant(CCodePart<?> part, Long constant) {
		return part.underlying().int64(constant);
	}

	@Override
	protected Long neg(Long value) {
		return -value;
	}

	@Override
	protected Long add(Long value1, Long value2) {
		return value1 + value2;
	}

	@Override
	protected Long sub(Long value1, Long value2) {
		return value1 - value2;
	}

	@Override
	protected Long mul(Long value1, Long value2) {
		return value1 * value2;
	}

	@Override
	protected Long div(Long value1, Long value2) {
		return value1 / value2;
	}

	@Override
	protected Long rem(Long value1, Long value2) {
		return value1 % value2;
	}

	@Override
	protected int cmp(Long value1, Long value2) {
		return value1.compareTo(value2);
	}

	@Override
	protected Long shl(Long value, int numBits) {
		return value << numBits;
	}

	@Override
	protected Long lshr(Long value, int numBits) {
		return value >>> numBits;
	}

	@Override
	protected Long ashr(Long value, int numBits) {
		return value >> numBits;
	}

	@Override
	protected Long and(Long value1, Long value2) {
		return value1 & value2;
	}

	@Override
	protected Long or(Long value1, Long value2) {
		return value1 | value2;
	}

	@Override
	protected Long xor(Long value1, Long value2) {
		return value1 ^ value2;
	}

	@Override
	protected Long comp(Long value) {
		return ~value;
	}

}
