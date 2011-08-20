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


public abstract class IntCOp<O extends IntOp<O>>
		extends NumCOp<O>
		implements IntOp<O> {

	public IntCOp(CCode<?> code, O underlying) {
		super(code, underlying);
	}

	@Override
	public final O atomicAdd(CodeId id, Code code, RecOp<?, O> to) {

		final CCode<?> ccode = cast(code);
		final RecCOp<?, O> cto = (RecCOp<?, O>) to;
		final O underlyingSum = getUnderlying().atomicAdd(
				id,
				ccode.getUnderlying(),
				cto.getUnderlying());

		return create(ccode, underlyingSum);
	}

	@Override
	public final O atomicSub(CodeId id, Code code, RecOp<?, O> from) {

		final CCode<?> ccode = cast(code);
		final RecCOp<?, O> cfrom = (RecCOp<?, O>) from;
		final O underlyingDiff = getUnderlying().atomicSub(
				id,
				ccode.getUnderlying(),
				cfrom.getUnderlying());

		return create(ccode, underlyingDiff);
	}

	@Override
	public final O shl(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingShl = getUnderlying().shl(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingShl);
	}

	@Override
	public final O shl(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingShl = getUnderlying().shl(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingShl);
	}

	@Override
	public final O lshr(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingLShr = getUnderlying().lshr(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingLShr);
	}

	@Override
	public final O lshr(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingLShr = getUnderlying().lshr(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingLShr);
	}

	@Override
	public final O ashr(CodeId id, Code code, O numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingAShr = getUnderlying().ashr(
				id,
				ccode.getUnderlying(),
				underlying(numBits));

		return create(ccode, underlyingAShr);
	}

	@Override
	public final O ashr(CodeId id, Code code, int numBits) {

		final CCode<?> ccode = cast(code);
		final O underlyingAShr = getUnderlying().ashr(
				id,
				ccode.getUnderlying(),
				numBits);

		return create(ccode, underlyingAShr);
	}

	@Override
	public final O and(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		final O underlyingAnd = getUnderlying().and(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingAnd);
	}

	@Override
	public final O or(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		final O underlyingOr = getUnderlying().or(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingOr);
	}

	@Override
	public final O xor(CodeId id, Code code, O operand) {

		final CCode<?> ccode = cast(code);
		final O underlyingXor = getUnderlying().xor(
				id,
				ccode.getUnderlying(),
				underlying(operand));

		return create(ccode, underlyingXor);
	}

	@Override
	public final O comp(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final O underlyingComp =
				getUnderlying().comp(id, ccode.getUnderlying());

		return create(ccode, underlyingComp);
	}

	@Override
	public final BoolOp lowestBit(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingLowestBit =
				getUnderlying().lowestBit(id, ccode.getUnderlying());

		return new BoolCOp(ccode, underlyingLowestBit);
	}

}
