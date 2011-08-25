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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.rec.RecCOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.codegen.code.op.RecOp;


public abstract class IntCOp<O extends IntOp<O>, T extends Number>
		extends NumCOp<O, T>
		implements IntOp<O> {

	public IntCOp(CCode<?> code, O underlying, T constant) {
		super(code, underlying, constant);
	}

	@Override
	public final O atomicAdd(CodeId id, Code code, RecOp<?, O> to) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final RecCOp<?, O, T> cto = (RecCOp<?, O, T>) to;
		final O underlyingSum = getUnderlying().atomicAdd(
				id,
				ccode.getUnderlying(),
				cto.getUnderlying());

		return create(ccode, underlyingSum, null);
	}

	@Override
	public final O atomicSub(CodeId id, Code code, RecOp<?, O> from) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final RecCOp<?, O, T> cfrom = (RecCOp<?, O, T>) from;
		final O underlyingDiff = getUnderlying().atomicSub(
				id,
				ccode.getUnderlying(),
				cfrom.getUnderlying());

		return create(ccode, underlyingDiff, null);
	}

	@Override
	public final O shl(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> nb = (IntCOp<O, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T shl = shl(getConstant(), nb.getConstant().intValue());

			return create(ccode, underlyingConstant(ccode, shl), shl);
		}

		final O underlyingShl = getUnderlying().shl(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingShl, null);
	}

	@Override
	public final O shl(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T shl = shl(getConstant(), Integer.valueOf(numBits));

			return create(ccode, underlyingConstant(ccode, shl), shl);
		}

		final O underlyingShl = getUnderlying().shl(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingShl, null);
	}

	@Override
	public final O lshr(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> nb = (IntCOp<O, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T lshr = lshr(getConstant(), nb.getConstant().intValue());

			return create(ccode, underlyingConstant(ccode, lshr), lshr);
		}

		final O underlyingLShr = getUnderlying().lshr(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingLShr, null);
	}

	@Override
	public final O lshr(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T lshr = lshr(getConstant(), Integer.valueOf(numBits));

			return create(ccode, underlyingConstant(ccode, lshr), lshr);
		}

		final O underlyingLShr = getUnderlying().lshr(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingLShr, null);
	}

	@Override
	public final O ashr(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> nb = (IntCOp<O, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T ashr = ashr(getConstant(), nb.getConstant().intValue());

			return create(ccode, underlyingConstant(ccode, ashr), ashr);
		}

		final O underlyingAShr = getUnderlying().ashr(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingAShr, null);
	}

	@Override
	public final O ashr(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T ashr = ashr(getConstant(), Integer.valueOf(numBits));

			return create(ccode, underlyingConstant(ccode, ashr), ashr);
		}

		final O underlyingAShr = getUnderlying().ashr(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingAShr, null);
	}

	@Override
	public final O and(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> op = (IntCOp<O, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T and = and(getConstant(), op.getConstant());

			return create(ccode, underlyingConstant(ccode, and), and);
		}

		final O underlyingAnd = getUnderlying().and(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingAnd, null);
	}

	@Override
	public final O or(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> op = (IntCOp<O, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T or = or(getConstant(), op.getConstant());

			return create(ccode, underlyingConstant(ccode, or), or);
		}

		final O underlyingOr = getUnderlying().or(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingOr, null);
	}

	@Override
	public final O xor(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<O, T> op = (IntCOp<O, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T xor = xor(getConstant(), op.getConstant());

			return create(ccode, underlyingConstant(ccode, xor), xor);
		}

		final O underlyingXor = getUnderlying().xor(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingXor, null);
	}

	@Override
	public final O comp(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T comp = comp(getConstant());

			return create(ccode, underlyingConstant(ccode, comp), comp);
		}

		final O underlyingComp =
				getUnderlying().comp(id, ccode.getUnderlying());

		return create(ccode, underlyingComp, null);
	}

	@Override
	public final BoolOp lowestBit(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final boolean lowestBit = (getConstant().intValue() & 1) != 0;

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(lowestBit),
					lowestBit);
		}

		final BoolOp underlyingLowestBit =
				getUnderlying().lowestBit(id, ccode.getUnderlying());

		return new BoolCOp(ccode, underlyingLowestBit, null);
	}

	protected abstract T shl(T value, int numBits);

	protected abstract T lshr(T value, int numBits);

	protected abstract T ashr(T value, int numBits);

	protected abstract T and(T value1, T value2);

	protected abstract T or(T value1, T value2);

	protected abstract T xor(T value1, T value2);

	protected abstract T comp(T value);

}
