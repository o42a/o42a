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
package org.o42a.backend.constant.data.struct;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.StructCDAlloc;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Allocated;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;


public class SubCDAlloc<S extends StructOp<S>> extends StructCDAlloc<S> {

	private final SubData<?> data;

	public SubCDAlloc(
			ContainerCDAlloc<?> enclosing,
			SubData<S> data,
			ContainerCDAlloc<S> typeAllocation) {
		super(enclosing, typeAllocation);
		this.data = data;
	}

	public SubCDAlloc(ConstBackend backend, Ptr<S> underlyingPtr) {
		super(backend, underlyingPtr);
		this.data = null;
	}

	public final SubData<?> getData() {
		return this.data;
	}

	@SuppressWarnings("unchecked")
	public final CType<S> getUnderlyingInstance() {
		return (CType<S>) getUnderlyingAllocated().getContainer();
	}

	@Override
	protected Allocated<S, ?> startUnderlyingAllocation(SubData<?> container) {

		final CType<S> underlyingType = getTypeAllocation().getUnderlyingType();

		return container.allocate(getData().getId().getLocal(), underlyingType);
	}

}
