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
package org.o42a.backend.constant.code.rec;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.PtrCOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.RecOp;


public abstract class RecCOp<R extends RecOp<R, O>, O extends Op>
		extends PtrCOp<R>
		implements RecOp<R, O> {

	public RecCOp(CCode<?> code, R underlying) {
		super(code, underlying);
	}

	@Override
	public final O load(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final O underlyingLoaded =
				getUnderlying().load(id, ccode.getUnderlying());

		return loaded(ccode, underlyingLoaded);
	}

	@Override
	public final void store(Code code, O value) {
		getUnderlying().store(underlying(code), underlying(value));
	}

	protected abstract O loaded(CCode<?> code, O underlying);

}
