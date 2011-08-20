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
package org.o42a.backend.constant.data.rec;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.AnyCOp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.backend.DataAllocation;


public final class AnyCDAlloc extends PtrRecCDAlloc<AnyPtrRec, AnyOp> {

	public AnyCDAlloc(
			ContainerCDAlloc<?> enclosing,
			AnyPtrRec data,
			AnyCDAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
	}

	public AnyCDAlloc(ContainerCDAlloc<?> enclosing, Ptr<AnyOp> underlyingPtr) {
		super(enclosing, underlyingPtr);
	}

	public AnyCDAlloc(ConstBackend backend, Ptr<AnyOp> underlyingPtr) {
		super(backend, underlyingPtr);
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return this;
	}

	@Override
	protected AnyPtrRec allocateUnderlying(SubData<?> container, String name) {
		return container.addPtr(name);
	}

	@Override
	protected AnyOp op(CCode<?> code, AnyOp underlyingOp) {
		return new AnyCOp(code, underlyingOp);
	}

}
