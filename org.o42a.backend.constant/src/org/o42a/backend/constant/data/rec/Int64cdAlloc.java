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
import org.o42a.backend.constant.code.rec.Int64recCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.data.Int64rec;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;


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
	public void setValue(Long value) {
		super.setValue(value);
		this.nativePtr = null;
	}

	public void setNativePtr(Ptr<AnyOp> pointer) {
		this.nativePtr = pointer;
		super.setValue(null);
	}

	@Override
	public void fill(Int64rec instance) {
		if (this.nativePtr != null) {
			instance.setNativePtr(this.nativePtr);
		} else {
			instance.setValue(getValue());
		}
	}

	@Override
	protected Int64recCOp op(CCode<?> code, Int64recOp underlying) {
		return new Int64recCOp(code, underlying);
	}

	@Override
	protected Int64rec allocateUnderlying(SubData<?> container, String name) {
		return container.addInt64(name, this);
	}
}
