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
import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;
import static org.o42a.core.ir.object.ObjectOp.approximateObject;

import java.util.function.Function;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class ByOwnerStoreOp implements FldStoreOp {

	private static final AllocatableOwnerPtr ALLOCATABLE_OWNER_PTR =
			new AllocatableOwnerPtr();

	private final ID id;
	private final Allocator allocator;
	private final Function<CodeDirs, DataRecOp> getOwnerPtr;

	public ByOwnerStoreOp(ID id, Code code) {
		this.id = id;
		this.allocator = code.getAllocator();

		final Allocated<DataRecOp> allocated =
				code.allocate(id, ALLOCATABLE_OWNER_PTR);

		this.getOwnerPtr = dirs -> allocated.get(dirs.code());
	}

	public ByOwnerStoreOp(ID id, Function<CodeDirs, LocalIROp> getLocal) {
		this.id = id;
		this.allocator = null;
		this.getOwnerPtr =
				dirs -> getLocal.apply(dirs).ptr().object(id, dirs.code());
	}

	public abstract Obj getOwnerType();

	public final boolean isMemberStore() {
		return this.allocator == null;
	}

	@Override
	public final void storeTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp owner = owner(dirs, this.allocator);

		ownerPtr(dirs).store(
				code,
				owner.toData(null, code),
				isMemberStore() ? ACQUIRE_RELEASE : NOT_ATOMIC);
	}

	@Override
	public final ObjectOp loadOwner(CodeDirs dirs) {

		final Block code = dirs.code();

		return approximateObject(
				dirs,
				ownerPtr(dirs).load(null, code),
				getOwnerType());
	}

	@Override
	public final HostOp loadTarget(CodeDirs dirs) {

		final ObjOp owner = loadOwner(dirs).castToWellKnown(null, dirs);

		return op(dirs, owner);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	protected abstract ObjectOp owner(CodeDirs dirs, Allocator allocator);

	protected abstract HostOp op(CodeDirs dirs, ObjOp owner);

	private final DataRecOp ownerPtr(CodeDirs dirs) {
		return this.getOwnerPtr.apply(dirs);
	}

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
