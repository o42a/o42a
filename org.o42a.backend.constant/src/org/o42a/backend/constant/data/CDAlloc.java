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

import org.o42a.backend.constant.data.rec.PtrRecCDAlloc;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;


public abstract class CDAlloc<P extends PtrOp<P>, D extends Data<P>>
		implements DataAllocation<P> {

	private final ConstBackend backend;
	private final CDAlloc<P, D> typeAllocation;
	private D underlying;
	private Ptr<P> underlyingPtr;
	private final D data;
	private int index;

	public CDAlloc(
			ConstBackend backend,
			D data,
			CDAlloc<P, D> typeAllocation) {
		this.backend = backend;
		this.data = data;
		this.typeAllocation = typeAllocation;
	}

	public CDAlloc(ConstBackend backend, Ptr<P> underlyingPtr) {
		this.backend = backend;
		this.data = null;
		this.typeAllocation = null;
		this.underlying = null;
		this.underlyingPtr = underlyingPtr;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	public CDAlloc<P, D> getTypeAllocation() {
		return this.typeAllocation;
	}

	public final D getData() {
		return this.data;
	}

	public final Ptr<P> getPointer() {
		return this.data != null ? this.data.getPointer() : null;
	}

	public final boolean isUnderlyingAllocated() {
		return this.underlying != null;
	}

	public abstract TopLevelCDAlloc<?> getTopLevel();

	public abstract ContainerCDAlloc<?> getEnclosing();

	public final int getIndex() {
		return this.index;
	}

	public D getUnderlying() {
		if (this.underlying == null) {
			assert this.underlyingPtr == null :
				"Can not allocate " + this;
			getTopLevel().initUnderlying(null);
		}
		return this.underlying;
	}

	public Ptr<P> getUnderlyingPtr() {
		if (this.underlyingPtr != null) {
			return this.underlyingPtr;
		}
		return this.underlyingPtr = getUnderlying().getPointer();
	}

	@Override
	public final DataLayout getLayout() {

		final CDAlloc<P, D> typeAllocation = getTypeAllocation();

		if (typeAllocation != null) {
			return typeAllocation.getLayout();
		}

		return getUnderlyingPtr().getAllocation().getLayout();
	}

	@Override
	public RelAllocation relativeTo(
			RelPtr pointer,
			DataAllocation<?> allocation) {
		return new RelCDAlloc(pointer, (CDAlloc<?, ?>) allocation, this);
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return new AnyCDAlloc(getBackend(), getUnderlyingPtr().toAny());
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return new DataCDAlloc(getBackend(), getUnderlyingPtr().toData());
	}

	@Override
	public <RR extends RecOp<RR, PP>, PP extends P> void write(
			DataWriter writer,
			DataAllocation<RR> destination) {

		@SuppressWarnings("unchecked")
		final PtrRecCDAlloc<PtrRec<RR, Ptr<P>>, RR, Ptr<P>> dest =
				(PtrRecCDAlloc<PtrRec<RR, Ptr<P>>, RR, Ptr<P>>) destination;

		dest.setConstant(dest.getData().isConstant());
		dest.setValue(getUnderlyingPtr());
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return super.toString();
		}
		return this.data.toString();
	}

	protected final void nest() {
		this.index = getEnclosing().nest(this);
	}

	protected abstract D allocateUnderlying(SubData<?> container);

	void initUnderlying(SubData<?> container) {
		this.underlying = allocateUnderlying(container);
		assert this.underlying != null :
			"Failed to allocate underlying data for " + this;
		assert (this.underlying.getGenerator()
				== getBackend().getUnderlyingGenerator()):
					"Wrong underlying generator of " + this.underlying;
	}

}
