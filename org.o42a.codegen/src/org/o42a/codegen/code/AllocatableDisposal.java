/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import static org.o42a.codegen.code.AllocationMode.NO_ALLOCATION;


class AllocatableDisposal implements Allocatable<Void> {

	private final Disposal disposal;
	private final int priority;

	AllocatableDisposal(Disposal disposal) {
		this.disposal = disposal;
		this.priority = NORMAL_DISPOSE_PRIORITY;
	}

	AllocatableDisposal(Disposal disposal, int priority) {
		this.disposal = disposal;
		this.priority = priority;
	}

	@Override
	public AllocationMode getAllocationMode() {
		return NO_ALLOCATION;
	}

	@Override
	public int getDisposePriority() {
		return this.priority;
	}

	@Override
	public Void allocate(Allocations code, Allocated<Void> allocated) {
		return null;
	}

	@Override
	public void init(Code code, Void allocated) {
	}

	@Override
	public void dispose(Code code, Allocated<Void> allocated) {
		this.disposal.dispose(code);
	}

	@Override
	public String toString() {
		if (this.disposal == null) {
			return super.toString();
		}
		return this.disposal.toString();
	}

}
