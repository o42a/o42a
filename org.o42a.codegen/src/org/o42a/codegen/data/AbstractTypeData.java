/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;


abstract class AbstractTypeData<O extends StructOp> extends SubData<O> {

	private boolean scheduled;

	AbstractTypeData(Generator generator, CodeId id, Type<O> instance) {
		super(generator, id, instance);
	}

	@Override
	public String toString() {
		return getInstance().toString();
	}

	@Override
	protected final void allocate(DataAllocator allocator) {
		if (begin(true)) {
			end(true);
		}
	}

	protected abstract DataAllocation<O> beginTypeAllocation(
			DataAllocator allocator);

	protected abstract void endTypeAllocation(DataAllocator allocator);

	protected void postTypeAllocation() {
	}

	@Override
	final void allocateType(boolean fully) {

		final boolean immediately = fully || getInstance().isReentrant();

		if (!begin(immediately)) {
			return;
		}
		if (immediately) {
			end(true);
			return;
		}

		final Globals globals = getGenerator();

		globals.scheduleTypeAllocation(this);
	}

	final boolean end(boolean immediately) {
		if (!immediately) {
			// Request by scheduler.
			if (!this.scheduled) {
				// Already allocated.
				return false;
			}
		}

		this.scheduled = false; // Prevent double allocation.

		final Globals globals = getGenerator();

		getInstance().allocateInstance(this);
		endTypeAllocation(globals.dataAllocator());
		getInstance().setAllocated(getGenerator());
		globals.allocatedType(this, immediately);
		postTypeAllocation();

		return true;
	}

	private boolean begin(boolean immediately) {
		if (immediately && this.scheduled) {
			// Already scheduled for allocation,
			// but immediate allocation requested.
			end(false);
			return false;
		}
		if (!getInstance().startAllocation(getGenerator())) {
			return false;
		}
		this.scheduled = !immediately;

		final Globals globals = getGenerator();

		if (immediately) {
			globals.allocatingType(this);
		}
		setAllocation(beginTypeAllocation(getGenerator().dataAllocator()));

		globals.registerType(this);

		return true;
	}

}
