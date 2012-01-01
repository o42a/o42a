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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.AnyCOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;


public final class AnyCDAlloc extends CDAlloc<AnyOp, Data<AnyOp>> {

	public AnyCDAlloc(ConstBackend backend, Ptr<AnyOp> underlyingPtr) {
		super(backend, underlyingPtr);
	}

	@Override
	public AnyCOp op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);
		final AnyOp underlyingOp =
				getUnderlyingPtr().op(id, ccode.getUnderlying());

		return new AnyCOp(ccode, underlyingOp, null);
	}

	@Override
	public TopLevelCDAlloc<?> getTopLevel() {
		return null;
	}

	@Override
	public ContainerCDAlloc<?> getEnclosing() {
		return null;
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return this;
	}

	@Override
	protected Data<AnyOp> allocateUnderlying(SubData<?> container) {
		throw new UnsupportedOperationException();
	}

}
