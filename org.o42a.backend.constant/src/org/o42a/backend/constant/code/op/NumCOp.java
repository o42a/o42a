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
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;


public abstract class NumCOp<O extends NumOp<O>, T extends Number>
		extends AbstractCOp<O, T> implements NumOp<O> {

	public NumCOp(CCode<?> code, O underlying, T constant) {
		super(code, underlying, constant);
	}

	@Override
	public final O neg(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T neg = neg(getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, neg),
					neg);
		}

		return create(
				ccode,
				getUnderlying().neg(id, ccode.getUnderlying()),
				null);
	}

	@Override
	public final O add(CodeId id, Code code, O summand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> s = (NumCOp<O, T>) summand;

		if (isConstant() && s.isConstant()) {

			final T sum = add(getConstant(), s.getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, sum),
					sum);
		}

		final O underlyingSum = getUnderlying().add(
				id,
				ccode.getUnderlying(),
				s.getUnderlying());

		return create(ccode, underlyingSum, null);
	}

	@Override
	public final O sub(CodeId id, Code code, O subtrahend) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> s = (NumCOp<O, T>) subtrahend;

		if (isConstant() && s.isConstant()) {

			final T diff = sub(getConstant(), s.getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, diff),
					diff);
		}

		final O underlyingDiff = getUnderlying().sub(
				id,
				ccode.getUnderlying(),
				s.getUnderlying());

		return create(ccode, underlyingDiff, null);
	}

	@Override
	public final O mul(CodeId id, Code code, O multiplier) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> m = (NumCOp<O, T>) multiplier;

		if (isConstant() && m.isConstant()) {

			final T mul = mul(getConstant(), m.getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, mul),
					mul);
		}

		final O underlyingMul = getUnderlying().mul(
				id,
				ccode.getUnderlying(),
				m.getUnderlying());

		return create(ccode, underlyingMul, null);
	}

	@Override
	public final O div(CodeId id, Code code, O divisor) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> d = (NumCOp<O, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T div = div(getConstant(), d.getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, div),
					div);
		}

		final O underlyingDiv = getUnderlying().div(
				id,
				ccode.getUnderlying(),
				d.getUnderlying());

		return create(ccode, underlyingDiv, null);
	}

	@Override
	public final O rem(CodeId id, Code code, O divisor) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> d = (NumCOp<O, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T rem = rem(getConstant(), d.getConstant());

			return create(
					ccode,
					underlyingConstant(ccode, rem),
					rem);
		}

		final O underlyingRem = getUnderlying().rem(
				id,
				ccode.getUnderlying(),
				underlying(divisor));

		return create(ccode, underlyingRem, null);
	}

	@Override
	public final BoolOp eq(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean eq = getConstant().equals(o.getConstant());

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(eq),
					eq);
		}

		final BoolOp underlyingEq = getUnderlying().eq(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingEq, null);
	}

	@Override
	public final BoolOp ne(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ne = !getConstant().equals(o.getConstant());

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(ne),
					ne);
		}

		final BoolOp underlyingNe = getUnderlying().ne(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingNe, null);
	}

	@Override
	public final BoolOp gt(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean gt = cmp(getConstant(), o.getConstant()) > 0;

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(gt),
					gt);
		}

		final BoolOp underlyingGt = getUnderlying().gt(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingGt, null);
	}

	@Override
	public BoolOp ge(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ge = cmp(getConstant(), o.getConstant()) >= 0;

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(ge),
					ge);
		}

		final BoolOp underlyingGe = getUnderlying().ge(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingGe, null);
	}

	@Override
	public BoolOp lt(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean lt = cmp(getConstant(), o.getConstant()) < 0;

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(lt),
					lt);
		}

		final BoolOp underlyingLt = getUnderlying().lt(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingLt, null);
	}

	@Override
	public BoolOp le(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<O, T> o = (NumCOp<O, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean le = cmp(getConstant(), o.getConstant()) <= 0;

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(le),
					le);
		}

		final BoolOp underlyingLe = getUnderlying().le(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingLe, null);
	}

	@Override
	public final Int8op toInt8(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final byte int8 = getConstant().byteValue();

			return new Int8cOp(
					ccode,
					ccode.getUnderlying().int8(int8),
					int8);
		}

		final Int8op underlyingInt8 =
				getUnderlying().toInt8(id, ccode.getUnderlying());

		return new Int8cOp(ccode, underlyingInt8, null);
	}

	@Override
	public final Int16op toInt16(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final short int16 = getConstant().shortValue();

			return new Int16cOp(
					ccode,
					ccode.getUnderlying().int16(int16),
					int16);
		}

		final Int16op underlyingInt16 =
				getUnderlying().toInt16(id, ccode.getUnderlying());

		return new Int16cOp(ccode, underlyingInt16, null);
	}

	@Override
	public final Int32op toInt32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final int int32 = getConstant().intValue();

			return new Int32cOp(
					ccode,
					ccode.getUnderlying().int32(int32),
					int32);
		}

		final Int32op underlyingInt32 =
				getUnderlying().toInt32(id, ccode.getUnderlying());

		return new Int32cOp(ccode, underlyingInt32, null);
	}

	@Override
	public final Int64op toInt64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final long int64 = getConstant().longValue();

			return new Int64cOp(
					ccode,
					ccode.getUnderlying().int64(int64),
					int64);
		}

		final Int64op underlyingInt64 =
				getUnderlying().toInt64(id, ccode.getUnderlying());

		return new Int64cOp(ccode, underlyingInt64, null);
	}

	@Override
	public final Fp32op toFp32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final float fp32 = getConstant().floatValue();

			return new Fp32cOp(
					ccode,
					ccode.getUnderlying().fp32(fp32),
					fp32);
		}

		final Fp32op underlyingFp32 =
				getUnderlying().toFp32(id, ccode.getUnderlying());

		return new Fp32cOp(ccode, underlyingFp32, null);
	}

	@Override
	public final Fp64op toFp64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final double fp64 = getConstant().doubleValue();

			return new Fp64cOp(
					ccode,
					ccode.getUnderlying().fp64(fp64),
					fp64);
		}

		final Fp64op underlyingFp64 =
				getUnderlying().toFp64(id, ccode.getUnderlying());

		return new Fp64cOp(ccode, underlyingFp64, null);
	}

	protected abstract O underlyingConstant(CCode<?> code, T constant);

	protected abstract T neg(T value);

	protected abstract T add(T value1, T value2);

	protected abstract T sub(T value1, T value2);

	protected abstract T mul(T value1, T value2);

	protected abstract T div(T value1, T value2);

	protected abstract T rem(T value1, T value2);

	protected abstract int cmp(T value1, T value2);

	@Override
	public final void returnValue(Code code) {

		final CCode<?> ccode = cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

}
