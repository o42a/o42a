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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.struct.StructStore.allocStructStore;
import static org.o42a.util.fn.Init.init;

import java.util.ArrayList;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public abstract class ContainerCDAlloc<S extends StructOp<S>>
		extends DCDAlloc<S, SubData<S>> {

	private final ArrayList<DCDAlloc<?, ?>> nested = new ArrayList<>();
	private final CType<S> underlyingStruct;
	private Allocated<S, ?> underlyingAllocated;
	private final Init<ContainerCDAlloc<S>> declaringType =
			init(this::determineDeclaringType);
	private boolean containerAllocated;

	public ContainerCDAlloc(
			ConstBackend backend,
			SubData<S> data,
			ContainerCDAlloc<S> typeAllocation,
			CType<S> underlyingStruct) {
		super(backend, data, typeAllocation);
		this.underlyingStruct = underlyingStruct;
	}

	public ContainerCDAlloc(
			ConstBackend backend,
			Ptr<S> pointer,
			UnderAlloc<S> underlAlloc) {
		super(backend, pointer, underlAlloc);
		this.underlyingStruct = null;
	}

	public final Type<S> getType() {
		return getUnderlyingInstance().getOriginal().getType();
	}

	@Override
	public final ContainerCDAlloc<S> getTypeAllocation() {
		return (ContainerCDAlloc<S>) super.getTypeAllocation();
	}

	public ContainerCDAlloc<S> getDeclaringType() {
		return this.declaringType.get();
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
		return getUnderlyingStruct() != null;
	}

	public final CType<S> getUnderlyingStruct() {
		return this.underlyingStruct;
	}

	@SuppressWarnings("unchecked")
	public <P extends AllocPtrOp<P>, D extends Data<P>> DCDAlloc<P, D> field(
			D field) {
		return (DCDAlloc<P, D>) field(field.getPointer());
	}

	@Override
	public S op(ID id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);
		final Type<S> type;

		if (isStruct()) {
			type = getUnderlyingStruct().getOriginal();
		} else {
			type = getTypeAllocation().getUnderlyingInstance().getOriginal();
		}

		return type.op(new CStruct<>(
				new OpBE<S>(id, ccode) {
					@Override
					public void prepare() {
					}
					@Override
					protected S write() {
						return getUnderlying().getPointer().op(
								getId(),
								part().underlying());
					}
				},
				allocStructStore(allocClass.allocPlace(ccode.code())),
				type,
				getPointer()));
	}

	@Override
	protected final SubData<S> allocateUnderlying(SubData<?> container) {
		this.underlyingAllocated = startUnderlyingAllocation(container);
		return this.underlyingAllocated.getData();
	}

	@Override
	protected void initUnderlying(SubData<?> container) {
		super.initUnderlying(container);

		final SubData<S> underlying = getUnderlying();

		for (DCDAlloc<?, ?> nested : this.nested) {
			nested.initUnderlying(underlying);
		}
		if (isContainerAllocated()) {
			this.underlyingAllocated.done();
		}
	}

	protected abstract Allocated<S, ?> startUnderlyingAllocation(
			SubData<?> container);

	final int nest(DCDAlloc<?, ?> nested) {

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

	private ContainerCDAlloc<S> determineDeclaringType() {

		final ContainerCDAlloc<S> typeAlloc;

		if (isStruct()) {
			typeAlloc = (ContainerCDAlloc<S>) getUnderlyingStruct()
					.getOriginal().pointer(
							getBackend().getGenerator()).getAllocation();
		} else {
			typeAlloc = getTypeAllocation();
			if (typeAlloc == null) {
				return this;
			}
		}

		if (typeAlloc == this) {
			return this;
		}

		return typeAlloc.getDeclaringType();
	}

	private final DCDAlloc<?, ?> field(Ptr<?> pointer) {

		final DCDAlloc<?, ?> allocation =
				(DCDAlloc<?, ?>) pointer.getAllocation();
		final DCDAlloc<?, ?> field = field(allocation);

		assert field != null :
			pointer + " is not inside of " + this;

		return field;
	}

	private DCDAlloc<?, ?> field(DCDAlloc<?, ?> alloc) {

		final ContainerCDAlloc<?> enclosing = alloc.getEnclosing();

		if (enclosing == null) {
			return null;
		}
		if (hasSameType(enclosing)) {
			return this.nested.get(alloc.getIndex());
		}

		final ContainerCDAlloc<?> enclosingField =
				(ContainerCDAlloc<?>) field(enclosing);

		assert enclosingField != null :
			"No such field: " + alloc + " in " + this;

		return enclosingField.nested.get(alloc.getIndex());
	}

	private boolean hasSameType(ContainerCDAlloc<?> alloc) {
		return alloc.getDeclaringType() == getDeclaringType();
	}

}
