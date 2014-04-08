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


class AllocatableDisposal implements Allocatable<Void> {

	private final Disposal disposal;
	private final int priority;

	AllocatableDisposal(Disposal disposal) {
		this.disposal = disposal;
		this.priority = NORMAL_ALLOC_PRIORITY;
	}

	AllocatableDisposal(Disposal disposal, int priority) {
		this.disposal = disposal;
		this.priority = priority;
	}

	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public Void allocate(AllocationCode<Void> code) {
		return null;
	}

	@Override
	public void initialize(AllocationCode<Void> code) {
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
