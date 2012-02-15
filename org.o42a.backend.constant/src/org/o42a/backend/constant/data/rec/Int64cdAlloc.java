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
package org.o42a.backend.constant.data.rec;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.Int64recCOp;
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.data.*;


public final class Int64cdAlloc extends RecCDAlloc<Int64rec, Int64recOp, Long> {

	private Ptr<AnyOp> nativePtr;

	public Int64cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Int64rec data,
			Int64cdAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	public final void setValue(Long value) {
		super.setValue(value);
		this.nativePtr = null;
	}

	public final void setNativePtr(Ptr<AnyOp> pointer) {
		this.nativePtr = pointer;
		super.setValue(null);
	}

	@Override
	public Long underlyingValue(Long value) {
		return value;
	}

	@Override
	public void fill(Int64rec instance) {
		if (this.nativePtr != null) {

			final AnyCDAlloc alloc =
					(AnyCDAlloc) this.nativePtr.getAllocation();
			final Ptr<AnyOp> underlyingPtr = alloc.getUnderlyingPtr();

			instance.setNativePtr(underlyingPtr);
		} else {
			instance.setValue(getValue());
		}
	}

	@Override
	protected Int64rec allocateUnderlying(SubData<?> container, String name) {
		return container.addInt64(name, this);
	}

	@Override
	protected Int64recOp op(OpBE<Int64recOp> backend, AllocClass allocClass) {
		return new Int64recCOp(backend, allocClass, getPointer());
	}

}
