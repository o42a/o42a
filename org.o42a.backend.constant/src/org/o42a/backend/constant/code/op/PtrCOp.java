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
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;


public abstract class PtrCOp<P extends PtrOp<P>>
		extends AbstractCOp<P>
		implements PtrOp<P> {

	public PtrCOp(CCode<?> code, P underlying) {
		super(code, underlying);
	}

	@Override
	public final AllocClass getAllocClass() {
		return getUnderlying().getAllocClass();
	}

	@Override
	public final void returnValue(Code code) {

		final CCode<?> ccode = ConstBackend.cast(code);

		ccode.beforeReturn();
		getUnderlying().returnValue(ccode.getUnderlying());
	}

	@Override
	public final BoolOp isNull(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingIsNull =
				getUnderlying().isNull(id, ccode.getUnderlying());

		return new BoolCOp(ccode, underlyingIsNull);
	}

	@Override
	public final BoolOp eq(CodeId id, Code code, P other) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingEq = getUnderlying().eq(
				id,
				ccode.getUnderlying(),
				underlying(other));

		return new BoolCOp(ccode, underlyingEq);
	}

	@Override
	public final P offset(CodeId id, Code code, IntOp<?> index) {

		final CCode<?> ccode = cast(code);
		final P underlyingOffset = getUnderlying().offset(
				id,
				ccode.getUnderlying(),
				underlying(index));

		return create(ccode, underlyingOffset);
	}

	@Override
	public AnyCOp toAny(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlyingAny =
				getUnderlying().toAny(id, ccode.getUnderlying());

		return new AnyCOp(ccode, underlyingAny);
	}

}
