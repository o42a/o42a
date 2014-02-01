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
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;


public final class StructRecCDAlloc<S extends StructOp<S>>
		extends PtrRecCDAlloc<StructRec<S>, StructRecOp<S>, Ptr<S>> {

	private final ContainerCDAlloc<S> structAllocation;

	public StructRecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			StructRec<S> data,
			StructRecCDAlloc<S> typeAllocation,
			ContainerCDAlloc<S> structAllocation) {
		super(enclosing, data, typeAllocation);
		this.structAllocation = structAllocation;
		nest();
	}

	public final ContainerCDAlloc<S> getStructAllocation() {
		return this.structAllocation;
	}

	public final CType<S> getUnderlyingType() {
		return getStructAllocation().getUnderlyingInstance();
	}

	public final Type<S> getType() {
		return getUnderlyingType().getOriginal();
	}

	@Override
	public Ptr<S> underlyingValue(Ptr<S> value) {

		final CDAlloc<S> alloc = (CDAlloc<S>) value.get().getAllocation();

		return alloc.getUnderlyingPtr();
	}

	@Override
	protected StructRec<S> allocateUnderlying(
			SubData<?> container,
			String name) {
		return container.addPtr(name, getUnderlyingType());
	}

	@Override
	protected StructRecOp<S> op(
			OpBE<StructRecOp<S>> backend,
			AllocPlace allocPlace) {
		return new StructRecCOp<>(
				backend,
				allocRecStore(allocPlace),
				getType(),
				getPointer());
	}

}
