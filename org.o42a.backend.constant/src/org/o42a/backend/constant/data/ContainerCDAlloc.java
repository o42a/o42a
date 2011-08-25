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

import java.util.ArrayList;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public abstract class ContainerCDAlloc<S extends StructOp<S>>
		extends CDAlloc<S, SubData<S>> {

	private final ArrayList<CDAlloc<?, ?>> nested =
			new ArrayList<CDAlloc<?,?>>();
	private final CType<S> underlyingStruct;
	private Allocated<S, ?> underlyingAllocated;
	private boolean containerAllocated;

	public ContainerCDAlloc(
			ConstBackend backend,
			SubData<S> data,
			ContainerCDAlloc<S> typeAllocation,
			CType<S> underlyingStruct) {
		super(backend, data, typeAllocation);
		this.underlyingStruct = underlyingStruct;
	}

	public ContainerCDAlloc(ConstBackend backend, Ptr<S> underlyingPtr) {
		super(backend, underlyingPtr);
		this.underlyingStruct = null;
	}

	@Override
	public ContainerCDAlloc<S> getTypeAllocation() {
		return (ContainerCDAlloc<S>) super.getTypeAllocation();
	}

	public final boolean isContainerAllocated() {
		return this.containerAllocated;
	}

	public final Allocated<S, ?> getUnderlyingAllocated() {
		getUnderlying();
		return this.underlyingAllocated;
	}

	public abstract CType<S> getUnderlyingInstance();

	public CType<S> getUnderlyingType() {
		return (CType<S>) getUnderlyingAllocated().getType();
	}

	public final boolean isStruct() {
		return getTypeAllocation() == null;
	}

	public final CType<S> getUnderlyingStruct() {
		return this.underlyingStruct;
	}

	@SuppressWarnings("unchecked")
	public <P extends PtrOp<P>, D extends Data<P>> CDAlloc<P, D> field(
			D field) {

		final CDAlloc<?, ?> alloc =
				(CDAlloc<?, ?>) field.getPointer().getAllocation();
		final ContainerCDAlloc<?> container =
				alloc.getEnclosing().findContainer(
						getTopLevel(),
						alloc.getEnclosing());

		return (CDAlloc<P, D>) container.nested.get(alloc.getIndex());
	}

	@Override
	public final S op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);
		final Type<S> type;

		if (isStruct()) {
			type = getUnderlyingStruct().getOriginal();
		} else {
			type = getTypeAllocation().getUnderlyingInstance().getOriginal();
		}

		return type.op(new CStruct<S>(
				ccode,
				getUnderlying().getPointer().op(id, ccode.getUnderlying()),
				type,
				getPointer()));
	}

	@Override
	protected final SubData<S> allocateUnderlying(SubData<?> container) {
		this.underlyingAllocated = startUnderlyingAllocation(container);
		return this.underlyingAllocated.getData();
	}

	protected abstract Allocated<S, ?> startUnderlyingAllocation(
			SubData<?> container);

	final int nest(CDAlloc<?, ?> nested) {

		final int index = this.nested.size();

		this.nested.add(nested);
		if (isUnderlyingAllocated()) {
			nested.initUnderlying(getUnderlying());
		}

		return index;
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

	private ContainerCDAlloc<?> findContainer(
			TopLevelCDAlloc<?> topLevel,
			CDAlloc<?, ?> alloc) {

		final ContainerCDAlloc<?> enclosing = alloc.getEnclosing();

		if (enclosing == null) {
			return topLevel;
		}

		final ContainerCDAlloc<?> found =
				enclosing.findContainer(topLevel, enclosing);

		return (ContainerCDAlloc<?>) found.nested.get(alloc.getIndex());
	}

}
