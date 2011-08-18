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


public final class RelCOp extends AbstractCOp<RelOp> implements RelOp {

	public RelCOp(CCode<?> code, RelOp underlying) {
		super(code, underlying);
	}

	@Override
	public final AnyCOp offset(CodeId id, Code code, PtrOp<?> from) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlyingOffset = getUnderlying().offset(
				id,
				ccode.getUnderlying(),
				underlying(from));

		return new AnyCOp(ccode, underlyingOffset);
	}

	@Override
	public final Int32cOp toInt32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int32op underlyingInt32 =
				getUnderlying().toInt32(id, ccode.getUnderlying());

		return new Int32cOp(ccode, underlyingInt32);
	}

	@Override
	public final RelCOp create(CCode<?> code, RelOp underlying) {
		return new RelCOp(code, underlying);
	}

}
