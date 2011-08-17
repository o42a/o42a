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

import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class CDAlloc<P extends PtrOp<P>, D extends Data<?>>
		implements DataAllocation<P> {

	private final CDAlloc<P, D> typeAllocation;
	private D underlying;

	public CDAlloc(CDAlloc<P, D> typeAllocation) {
		this.typeAllocation = typeAllocation;
	}

	public CDAlloc<P, D> getTypeAllocation() {
		return this.typeAllocation;
	}

	public final boolean isUnderlyingAllocated() {
		return this.underlying != null;
	}

	public abstract TopLevelCDAlloc<?> getTopLevel();

	public abstract ContainerCDAlloc<?> getEnclosing();

	public final D getUnderlying() {
		if (this.underlying == null) {
			getTopLevel().initUnderlying(null);
		}
		return this.underlying;
	}

	@Override
	public DataLayout getLayout() {

		final CDAlloc<P, D> typeAllocation = getTypeAllocation();

		if (typeAllocation != null) {
			return typeAllocation.getLayout();
		}

		return getUnderlying().getLayout();
	}

	protected abstract D allocateUnderlying(SubData<?> container);

	void initUnderlying(SubData<?> container) {
		this.underlying = allocateUnderlying(container);
	}

}
