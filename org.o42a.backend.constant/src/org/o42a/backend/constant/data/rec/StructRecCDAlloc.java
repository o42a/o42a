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
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.backend.constant.data.struct.StructCDAlloc;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;
import org.o42a.util.func.Getter;


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
	public Getter<Ptr<S>> underlyingValue(final Getter<Ptr<S>> value) {
		return new Getter<Ptr<S>>() {
			@Override
			public Ptr<S> get() {

				final StructCDAlloc<S> alloc =
						(StructCDAlloc<S>) value.get().getAllocation();

				return alloc.getUnderlyingPtr();
			}
			@Override
			public String toString() {
				return String.valueOf(value);
			}
		};
	}

	@Override
	protected StructRec<S> allocateUnderlying(
			SubData<?> container,
			String name) {
		return container.addPtr(name, getUnderlyingType(), this);
	}

	@Override
	protected StructRecOp<S> op(
			OpBE<StructRecOp<S>> backend,
			AllocClass allocClass) {
		return new StructRecCOp<S>(
				backend,
				allocClass,
				getType(),
				getPointer());
	}

}
