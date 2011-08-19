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

import org.o42a.backend.constant.data.rec.AnyCDAlloc;
import org.o42a.backend.constant.data.rec.DataCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;


public abstract class CDAlloc<P extends PtrOp<P>, D extends Data<P>>
		implements DataAllocation<P> {

	private final CDAlloc<P, D> typeAllocation;
	private D underlying;
	private Ptr<P> underlyingPtr;

	public CDAlloc(CDAlloc<P, D> typeAllocation) {
		this.typeAllocation = typeAllocation;
	}

	public CDAlloc(Ptr<P> underlyingPtr) {
		this.typeAllocation = null;
		this.underlying = null;
		this.underlyingPtr = underlyingPtr;
	}

	public CDAlloc<P, D> getTypeAllocation() {
		return this.typeAllocation;
	}

	public final boolean isUnderlyingAllocated() {
		return this.underlying != null;
	}

	public abstract TopLevelCDAlloc<?> getTopLevel();

	public abstract ContainerCDAlloc<?> getEnclosing();

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
	public RelAllocation relativeTo(DataAllocation<?> allocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<AnyOp> toAny() {
		return new AnyCDAlloc(getEnclosing(), getUnderlyingPtr().toAny());
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return new DataCDAlloc(getEnclosing(), getUnderlyingPtr().toData());
	}

	@Override
	public final void write(DataWriter writer) {

		final DataWriter underlyingWriter =
				getTopLevel().getBackend().getUnderlyingBackend().dataWriter();

		getUnderlyingPtr().getAllocation().write(underlyingWriter);
	}

	protected abstract D allocateUnderlying(SubData<?> container);

	void initUnderlying(SubData<?> container) {
		this.underlying = allocateUnderlying(container);
	}

}
