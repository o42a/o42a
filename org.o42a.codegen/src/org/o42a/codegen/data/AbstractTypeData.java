/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.util.string.ID;


abstract class AbstractTypeData<S extends StructOp<S>> extends SubData<S> {

	private boolean scheduled;

	AbstractTypeData(Generator generator, ID id, Type<S> instance) {
		super(generator, id, instance);
	}

	@Override
	public String toString() {
		return getInstance().toString();
	}

	@Override
	protected final void allocateType(boolean fully) {

		final boolean immediately = fully || getInstance().isReentrant();

		if (!startAllocation(immediately)) {
			return;
		}
		if (immediately) {
			completeAllocation(true);
			return;
		}

		final Globals globals = getGenerator().getGlobals();

		globals.scheduleTypeAllocation(this);
	}

	@Override
	protected final boolean startAllocation(DataAllocator allocator) {
		return startAllocation(true);
	}

	@Override
	protected final void allocateContents() {
		this.scheduled = false; // Prevent double allocation.
		getInstance().allocateInstance(this);
	}

	@Override
	protected final void endAllocation(DataAllocator allocator) {
		endAllocation(true);
	}

	protected abstract DataAllocation<S> startTypeAllocation(
			DataAllocator allocator);

	protected abstract void endTypeAllocation(DataAllocator allocator);

	protected void postTypeAllocation() {
		getGenerator().getGlobals().registerType(this);
	}

	final boolean completeAllocation(boolean immediately) {
		if (!immediately) {
			// Request by scheduler.
			if (!this.scheduled) {
				// Already allocated.
				return false;
			}
		}

		allocateContents();
		endAllocation(immediately);

		return true;
	}

	private void endAllocation(boolean immediately) {

		final Globals globals = getGenerator().getGlobals();

		endTypeAllocation(globals.dataAllocator());
		getInstance().setAllocated(getGenerator());
		globals.allocatedType(immediately);
		postTypeAllocation();
	}

	private boolean startAllocation(boolean immediately) {
		if (immediately && this.scheduled) {
			// Already scheduled for allocation,
			// but immediate allocation requested.
			completeAllocation(false);
			return false;
		}
		if (!getInstance().startAllocation(getGenerator())) {
			return false;
		}
		this.scheduled = !immediately;

		final Globals globals = getGenerator().getGlobals();

		if (immediately) {
			globals.allocatingType();
		}
		setAllocation(startTypeAllocation(globals.dataAllocator()));

		return true;
	}

}
