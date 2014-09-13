/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;
import org.o42a.util.DataLayout;


public abstract class DCDAlloc<P extends AllocPtrOp<P>, D extends Data<P>>
		extends CDAlloc<P> {

	private final DCDAlloc<P, D> typeAllocation;
	private final D data;
	private D underlying;
	private int index;

	public DCDAlloc(
			ConstBackend backend,
			D data,
			DCDAlloc<P, D> typeAllocation) {
		super(backend, data.getPointer(), UnderAlloc.<P>defaultUnderAlloc());
		this.typeAllocation = typeAllocation;
		this.data = data;
	}

	public DCDAlloc(
			ConstBackend backend,
			Ptr<P> pointer,
			UnderAlloc<P> underAlloc) {
		super(backend, pointer, underAlloc);
		this.typeAllocation = null;
		this.data = null;
	}

	public abstract TopLevelCDAlloc<?> getTopLevel();

	public abstract ContainerCDAlloc<?> getEnclosing();

	public final int getIndex() {
		return this.index;
	}

	public DCDAlloc<P, D> getTypeAllocation() {
		return this.typeAllocation;
	}

	public final D getData() {
		return this.data;
	}

	public final boolean isUnderlyingAllocated() {
		return this.underlying != null;
	}

	public final D getUnderlying() {
		if (this.underlying == null) {
			assert underAlloc().isDefault() :
				"Can not allocate " + this;
			getTopLevel().initUnderlying(null);
		}
		return this.underlying;
	}

	@Override
	public DataLayout getLayout() {

		final DCDAlloc<P, D> typeAllocation = getTypeAllocation();

		if (typeAllocation != null) {
			return typeAllocation.getLayout();
		}

		return getUnderlyingPtr().getAllocation().getLayout();
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return super.toString();
		}
		return this.data.getId().toString();
	}

	protected final void nest() {
		this.index = getEnclosing().nest(this);
	}

	protected void initUnderlying(SubData<?> container) {
		this.underlying = allocateUnderlying(container);
		assert this.underlying != null :
			"Failed to allocate underlying data for " + this;
		assert (this.underlying.getGenerator()
				== getBackend().getUnderlyingGenerator()):
					"Wrong underlying generator of " + this.underlying;
	}

	protected abstract D allocateUnderlying(SubData<?> container);

}
