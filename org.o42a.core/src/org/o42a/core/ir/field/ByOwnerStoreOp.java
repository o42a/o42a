/*
    Compiler Core
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
package org.o42a.core.ir.field;

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class ByOwnerStoreOp implements FldStoreOp {

	private static final AllocatableOwnerPtr ALLOCATABLE_OWNER_PTR =
			new AllocatableOwnerPtr();

	private final Allocator allocator;
	private final Allocated<DataRecOp> ownerPtr;

	public ByOwnerStoreOp(ID id, Code code) {
		this.allocator = code.getAllocator();
		this.ownerPtr = code.allocate(id, ALLOCATABLE_OWNER_PTR);
	}

	public abstract Obj getOwnerType();

	public abstract ObjectOp owner();

	@Override
	public final void storeTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp owner = owner();

		tempObjHolder(this.allocator).holdVolatile(code, owner);

		this.ownerPtr.get(code).store(code, owner.toData(null, code));
	}

	@Override
	public final ObjectOp loadOwner(CodeDirs dirs) {

		final Block code = dirs.code();

		return anonymousObject(
				dirs,
				this.ownerPtr.get(code).load(null, code),
				getOwnerType());
	}

	@Override
	public final TargetOp loadTarget(CodeDirs dirs) {

		final ObjOp owner = loadOwner(dirs).castToWellKnown(null, dirs);

		return op(dirs, owner);
	}

	@Override
	public String toString() {
		if (this.ownerPtr == null) {
			return super.toString();
		}
		return this.ownerPtr.toString();
	}

	protected abstract TargetOp op(CodeDirs dirs, ObjOp owner);

	private static final class AllocatableOwnerPtr
			implements Allocatable<DataRecOp> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public DataRecOp allocate(
				Allocations code,
				Allocated<DataRecOp> allocated) {
			return code.allocateDataPtr(allocated.getId());
		}

		@Override
		public void init(Code code, DataRecOp allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<DataRecOp> allocated) {
		}

	}

}
