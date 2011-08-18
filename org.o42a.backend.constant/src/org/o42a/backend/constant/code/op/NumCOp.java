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


public abstract class NumCOp<O extends NumOp<O>>
		extends AbstractCOp<O> implements NumOp<O> {

	public NumCOp(CCode<?> code, O underlying) {
		super(code, underlying);
	}

	@Override
	public final O neg(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		return create(ccode, getUnderlying().neg(id, ccode.getUnderlying()));
	}

	@Override
	public final O add(CodeId id, Code code, O summand) {

		final CCode<?> ccode = cast(code);
		final O underlyingSum = getUnderlying().add(
				id,
				ccode.getUnderlying(),
				underlying(summand));

		return create(ccode, underlyingSum);
	}

	@Override
	public final O sub(CodeId id, Code code, O subtrahend) {

		final CCode<?> ccode = cast(code);
		final O underlyingDiff = getUnderlying().sub(
				id,
				ccode.getUnderlying(),
				underlying(subtrahend));

		return create(ccode, underlyingDiff);
	}

	@Override
	public final O mul(CodeId id, Code code, O multiplier) {

		final CCode<?> ccode = cast(code);
		final O underlyingMul = getUnderlying().mul(
				id,
				ccode.getUnderlying(),
				underlying(multiplier));

		return create(ccode, underlyingMul);
	}

	@Override
	public final O div(CodeId id, Code code, O divisor) {

		final CCode<?> ccode = cast(code);
		final O underlyingDiv = getUnderlying().div(
				id,
				ccode.getUnderlying(),
				underlying(divisor));

		return create(ccode, underlyingDiv);
	}

	@Override
	public final O rem(CodeId id, Code code, O divisor) {

		final CCode<?> ccode = cast(code);
		final O underlyingRem = getUnderlying().rem(
				id,
				ccode.getUnderlying(),
				underlying(divisor));

		return create(ccode, underlyingRem);
	}

	@Override
	public final BoolOp eq(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingEq = getUnderlying().eq(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingEq);
	}

	@Override
	public final BoolOp ne(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingNe = getUnderlying().ne(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingNe);
	}

	@Override
	public final BoolOp gt(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingGt = getUnderlying().gt(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingGt);
	}

	@Override
	public BoolOp ge(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingGe = getUnderlying().ge(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingGe);
	}

	@Override
	public BoolOp lt(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingLt = getUnderlying().lt(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingLt);
	}

	@Override
	public BoolOp le(CodeId id, Code code, O other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingLe = getUnderlying().le(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingLe);
	}

	@Override
	public final Int8op toInt8(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int8op underlyingInt8 =
				getUnderlying().toInt8(id, ccode.getUnderlying());

		return new Int8cOp(ccode, underlyingInt8);
	}

	@Override
	public final Int16op toInt16(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int16op underlyingInt16 =
				getUnderlying().toInt16(id, ccode.getUnderlying());

		return new Int16cOp(ccode, underlyingInt16);
	}

	@Override
	public final Int32op toInt32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int32op underlyingInt32 =
				getUnderlying().toInt32(id, ccode.getUnderlying());

		return new Int32cOp(ccode, underlyingInt32);
	}

	@Override
	public final Int64op toInt64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int64op underlyingInt64 =
				getUnderlying().toInt64(id, ccode.getUnderlying());

		return new Int64cOp(ccode, underlyingInt64);
	}

	@Override
	public final Fp32op toFp32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Fp32op underlyingFp32 =
				getUnderlying().toFp32(id, ccode.getUnderlying());

		return new Fp32cOp(ccode, underlyingFp32);
	}

	@Override
	public final Fp64op toFp64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Fp64op underlyingFp64 =
				getUnderlying().toFp64(id, ccode.getUnderlying());

		return new Fp64cOp(ccode, underlyingFp64);
	}

	@Override
	public final void returnValue(Code code) {

		final CCode<?> ccode = cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

}
