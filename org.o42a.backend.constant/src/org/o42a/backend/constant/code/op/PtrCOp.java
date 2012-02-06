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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CBlock;
import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.AllocClass;


public abstract class PtrCOp<P extends PtrOp<P>, PT extends AbstractPtr>
		extends AbstractCOp<P, PT>
		implements PtrOp<P> {

	public PtrCOp(CCode<?> code, P underlying, PT constant) {
		super(code, underlying, constant);
	}

	@Override
	public final AllocClass getAllocClass() {
		return getUnderlying().getAllocClass();
	}

	@Override
	public final void returnValue(Block code) {

		final CBlock<?> ccode = ConstBackend.cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

	@Override
	public final BoolCOp isNull(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final Boolean result = getConstant().isNull();

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(result),
					result);
		}

		final BoolOp underlyingIsNull =
				getUnderlying().isNull(id, ccode.getUnderlying());

		return new BoolCOp(ccode, underlyingIsNull, null);
	}

	@Override
	public final BoolCOp eq(CodeId id, Code code, P other) {

		final CCode<?> ccode = cast(code);
		final COp<P, ?> o = cast(other);

		if (isConstant() && o.isConstant()) {

			final Boolean result = getConstant().equals(o.getConstant());

			return new BoolCOp(
					ccode,
					ccode.getUnderlying().bool(result),
					result);
		}

		final BoolOp underlyingEq = getUnderlying().eq(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingEq, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final P offset(CodeId id, Code code, IntOp<?> index) {

		final CCode<?> ccode = cast(code);
		final IntCOp<?, ?> idx = (IntCOp<?, ?>) index;

		if (idx.isConstant() && idx.getConstant().intValue() == 0) {
			if (getCode() == ccode) {
				return (P) this;
			}
			return create(ccode, getUnderlying(), getConstant());
		}

		final P underlyingOffset = getUnderlying().offset(
				id,
				ccode.getUnderlying(),
				idx.getUnderlying());

		return create(ccode, underlyingOffset, null);
	}

	@Override
	public AnyCOp toAny(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlyingAny =
				getUnderlying().toAny(id, ccode.getUnderlying());

		return new AnyCOp(ccode, underlyingAny, null);
	}

}
