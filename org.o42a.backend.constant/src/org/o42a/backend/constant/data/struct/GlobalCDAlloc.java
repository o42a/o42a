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
package org.o42a.backend.constant.data.struct;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.TopLevelCDAlloc;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public class GlobalCDAlloc<S extends StructOp<S>>
		extends TopLevelCDAlloc<S> {

	private final Global<S, ?> global;

	public GlobalCDAlloc(
			ConstBackend backend,
			SubData<S> data,
			ContainerCDAlloc<S> typeAllocation,
			Global<S, ?> global) {
		super(
				backend,
				data,
				typeAllocation,
				typeAllocation != null
				? null : new CType<>(backend, global.getInstance()));
		this.global = global;
	}

	@Override
	public final CType<S> getUnderlyingInstance() {
		return (CType<S>) getUnderlyingGlobal().getInstance();
	}

	@SuppressWarnings("unchecked")
	public final Global<S, ?> getUnderlyingGlobal() {
		return (Global<S, ?>) getUnderlyingAllocated().getContainer();
	}

	@Override
	public String toString() {
		if (this.global == null) {
			return super.toString();
		}
		return this.global.getId().toString();
	}

	@Override
	protected Allocated<S, ?> startUnderlyingAllocation(
			SubData<?> container) {

		final GlobalSettings globalSettings =
				getBackend()
				.getUnderlyingGenerator()
				.newGlobal()
				.set(this.global);

		if (isStruct()) {
			return globalSettings.allocateStruct(getUnderlyingStruct());
		}

		final CType<S> underlyingType =
				getTypeAllocation().getUnderlyingInstance();

		return globalSettings.allocate(this.global.getId(), underlyingType);
	}

}
