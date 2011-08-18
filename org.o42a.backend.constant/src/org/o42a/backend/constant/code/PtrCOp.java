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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.AllocClass;


public abstract class PtrCOp<P extends PtrOp<P>>
		implements PtrOp<P>, COp<P> {

	private final P underlying;

	public PtrCOp(P underlying) {
		this.underlying = underlying;
	}

	@Override
	public final P getUnderlying() {
		return this.underlying;
	}

	@Override
	public final CodeId getId() {
		return getUnderlying().getId();
	}

	@Override
	public final void allocated(Code code, StructOp<?> enclosing) {
		getUnderlying().allocated(
				underlying(code),
				underlying(enclosing));
	}

	@Override
	public final AllocClass getAllocClass() {
		return getUnderlying().getAllocClass();
	}

	@Override
	public final void returnValue(Code code) {
		getUnderlying().returnValue(underlying(code));
	}

	@Override
	public final BoolOp isNull(CodeId id, Code code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final BoolOp eq(CodeId id, Code code, P other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final P offset(CodeId id, Code code, IntOp<?> index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final AnyOp toAny(CodeId id, Code code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

}
