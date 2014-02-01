/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.backend.constant.code.rec.RecStore.allocRecStore;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.DataRecCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.DataCDAlloc;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.*;


public final class DataRecCDAlloc
		extends PtrRecCDAlloc<DataRec, DataRecOp, Ptr<DataOp>> {

	public DataRecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			DataRec data,
			DataRecCDAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	public Ptr<DataOp> underlyingValue(Ptr<DataOp> value) {

		final DataCDAlloc alloc = (DataCDAlloc) value.get().getAllocation();

		return alloc.getUnderlyingPtr();
	}

	@Override
	protected DataRec allocateUnderlying(SubData<?> container, String name) {
		return container.addDataPtr(name);
	}

	@Override
	protected DataRecOp op(OpBE<DataRecOp> backend, AllocPlace allocPlace) {
		return new DataRecCOp(backend, allocRecStore(allocPlace), getPointer());
	}

}
