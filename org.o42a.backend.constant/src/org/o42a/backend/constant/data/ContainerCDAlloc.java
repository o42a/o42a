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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import java.util.LinkedList;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public abstract class ContainerCDAlloc<S extends StructOp<S>>
		extends CDAlloc<S, SubData<S>> {

	private final LinkedList<CDAlloc<?, ?>> nested =
			new LinkedList<CDAlloc<?,?>>();
	private Allocated<S, ?> underlyingAllocated;

	private boolean containerAllocated;

	public ContainerCDAlloc(
			ConstBackend backend,
			ContainerCDAlloc<S> typeAllocation) {
		super(backend, typeAllocation);
	}

	public ContainerCDAlloc(ConstBackend backend, Ptr<S> underlyingPtr) {
		super(backend, underlyingPtr);
	}

	@Override
	public ContainerCDAlloc<S> getTypeAllocation() {
		return (ContainerCDAlloc<S>) super.getTypeAllocation();
	}

	public final boolean isContainerAllocated() {
		return this.containerAllocated;
	}

	public final Allocated<S, ?> getUnderlyingAllocated() {
		return this.underlyingAllocated;
	}

	public CType<S> getUnderlyingType() {
		return (CType<S>) getUnderlyingAllocated().getType();
	}

	@Override
	public final S op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);
		final Type<S> type = getUnderlyingType().getOriginal();

		return type.op(new CStruct<S>(
				ccode,
				getUnderlying().getPointer().op(id, ccode.getUnderlying())));
	}

	@Override
	protected final SubData<S> allocateUnderlying(SubData<?> container) {
		this.underlyingAllocated = startUnderlyingAllocation(container);
		return this.underlyingAllocated.getData();
	}

	protected abstract Allocated<S, ?> startUnderlyingAllocation(
			SubData<?> container);

	final void nest(CDAlloc<?, ?> nested) {
		this.nested.add(nested);
		if (isUnderlyingAllocated()) {
			nested.initUnderlying(getUnderlying());
		}
	}

	final void containerAllocated() {
		this.containerAllocated = true;
		if (isUnderlyingAllocated()) {
			this.underlyingAllocated.done();
		}
	}

	@Override
	void initUnderlying(SubData<?> container) {
		super.initUnderlying(container);

		final SubData<S> underlying = getUnderlying();

		for (CDAlloc<?, ?> nested : this.nested) {
			nested.initUnderlying(underlying);
		}
		if (isContainerAllocated()) {
			this.underlyingAllocated.done();
		}
	}

}
