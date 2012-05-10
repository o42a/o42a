/*
    Compiler Code Generator
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.codegen.CodeIdFactory.DEFAULT_CODE_ID_FACTORY;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class SystemType {

	private CodeId id;
	private Generator generator;
	private DataAllocation<SystemOp> allocation;
	private Ptr<SystemOp> pointer;

	public final CodeId getId() {
		if (this.id != null) {
			return this.id;
		}
		return codeId(DEFAULT_CODE_ID_FACTORY);
	}

	public final DataAllocation<SystemOp> getAllocation() {
		return this.allocation;
	}

	public final CodeId codeId(Generator generator) {
		return codeId(generator.getCodeIdFactory());
	}

	public final CodeId codeId(CodeIdFactory factory) {

		final CodeId id = this.id;

		if (id != null && id.compatibleWith(factory)) {
			return id;
		}

		return this.id = buildCodeId(factory);
	}

	public final Ptr<SystemOp> pointer(Generator generator) {
		if (this.pointer != null) {
			return this.pointer;
		}
		return this.pointer = new SystemPtr(codeId(generator), this);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract CodeId buildCodeId(CodeIdFactory factory);

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

		SystemPtr(CodeId id, SystemType systemType) {
			super(id, false, false);
			this.systemType = systemType;
		}

		@Override
		protected DataAllocation<SystemOp> createAllocation() {
			return this.systemType.getAllocation();
		}

	}

}
