/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.string.ID;


public abstract class SystemType {

	private final ID id;
	private Generator generator;
	private DataAllocation<SystemOp> allocation;
	private Ptr<SystemOp> pointer;

	public SystemType(ID id) {
		this.id = id;
	}

	public final ID getId() {
		return this.id;
	}

	public final DataAllocation<SystemOp> getAllocation() {
		return this.allocation;
	}

	public final Ptr<SystemOp> getPointer() {
		if (this.pointer != null) {
			return this.pointer;
		}
		return this.pointer = new SystemPtr(this);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	final void allocate(Generator generator) {
		if (this.allocation != null && this.generator == generator) {
			return;
		}
		this.generator = generator;
		this.allocation =
				generator.getGlobals().dataAllocator().addSystemType(this);
	}

	private static final class SystemPtr extends Ptr<SystemOp> {

		private final SystemType systemType;

		SystemPtr(SystemType systemType) {
			super(systemType.getId(), false, false);
			this.systemType = systemType;
		}

		@Override
		protected DataAllocation<SystemOp> createAllocation() {
			return this.systemType.getAllocation();
		}

	}

}
