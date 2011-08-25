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
import org.o42a.backend.constant.code.op.DataCOp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.backend.DataAllocation;


public final class DataCDAlloc extends PtrRecCDAlloc<DataRec, DataOp> {

	public DataCDAlloc(
			ContainerCDAlloc<?> enclosing,
			DataRec data,
			DataCDAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	public DataCDAlloc(ConstBackend backend, Ptr<DataOp> underlyingPtr) {
		super(backend, underlyingPtr);
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return this;
	}

	@Override
	protected DataRec allocateUnderlying(SubData<?> container, String name) {
		return container.addDataPtr(name, this);
	}

	@Override
	protected DataCOp op(CCode<?> code, DataOp underlyingOp) {
		return new DataCOp(code, underlyingOp, getPointer());
	}

}
